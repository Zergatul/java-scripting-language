package com.zergatul.scripting.binding;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.*;
import com.zergatul.scripting.compiler.frames.Frame;
import com.zergatul.scripting.compiler.frames.LoopFrame;
import com.zergatul.scripting.compiler.frames.TryCatchFrame;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.*;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.*;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Binder {

    private final String code;
    private final CompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;
    private final CompilationParameters parameters;
    private final DeclarationTable declarationTable;
    private CompilerContext context;

    public Binder(ParserOutput input, CompilationParameters parameters) {
        this.code = input.code();
        this.unit = input.unit();
        this.diagnostics = input.diagnostics();
        this.parameters = parameters;
        this.declarationTable = new DeclarationTable();
        this.context = parameters.getContext();
    }

    public BinderOutput bind() {
        BoundCompilationUnitNode unit = bindCompilationUnit(this.unit);
        return new BinderOutput(code, unit, context, diagnostics);
    }

    private BoundCompilationUnitNode bindCompilationUnit(CompilationUnitNode node) {
        buildDeclarationTable(node);
        List<BoundCompilationUnitMemberNode> members = bindCompilationUnitMembers(node.members.members);
        pushFunctionScope(parameters.getReturnType(), parameters.isAsync());
        parameters.addFunctionalInterfaceParameters(context);
        BoundStatementsListNode statements = bindStatementList(node.statements);
        popScope();
        return new BoundCompilationUnitNode(
                node,
                new BoundCompilationUnitMembersListNode(node.members, members),
                statements);
    }

    private List<BoundCompilationUnitMemberNode> bindCompilationUnitMembers(List<CompilationUnitMemberNode> nodes) {
        List<BoundCompilationUnitMemberNode> boundMembers = new ArrayList<>();
        for (CompilationUnitMemberNode member : nodes) {
            boundMembers.add(switch (member.getNodeType()) {
                case STATIC_VARIABLE -> bindStaticVariable((StaticVariableNode) member);
                case FUNCTION -> bindFunction((FunctionNode) member);
                case CLASS_DECLARATION -> bindClass((ClassNode) member);
                case EXTENSION_DECLARATION -> bindExtension((ExtensionNode) member);
                case TYPE_ALIAS -> bindTypeAlias((TypeAliasNode) member);
                default -> throw new InternalException();
            });
        }
        return boundMembers;
    }

    private BoundStaticVariableNode bindStaticVariable(StaticVariableNode staticVariableNode) {
        StaticVariableDeclaration declaration = declarationTable.getStaticVariableDeclaration(staticVariableNode);

        BoundTypeNode typeNode = declaration.typeNode();
        DeclaredStaticVariable variable = new DeclaredStaticVariable(declaration.name(), typeNode.type, staticVariableNode.name.getRange());
        SymbolRef symbolRef = new ImmutableSymbolRef(variable);
        if (!declaration.hasError()) {
            context.addStaticSymbol(declaration.name(), symbolRef);
        }

        BoundExpressionNode expression;
        if (staticVariableNode.expression != null) {
            expression = convert(bindExpression(staticVariableNode.expression), typeNode.type);
        } else {
            expression = null;
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(staticVariableNode.name, symbolRef);

        return new BoundStaticVariableNode(
                staticVariableNode,
                typeNode,
                name,
                expression);
    }

    private BoundFunctionDeclarationNode bindFunction(FunctionNode functionNode) {
        FunctionDeclaration declaration = declarationTable.getFunctionDeclaration(functionNode);

        pushStaticFunctionScope(declaration.getReturnType(), declaration.isAsync());
        addParametersToContext(declaration.getParameters());

        if (!declaration.getGroup().hasError()) {
            SymbolRef groupSymbolRef = declaration.getGroup().getSymbolRef();
            if (!context.getParent().getStaticSymbols().contains(groupSymbolRef)) {
                context.getParent().addStaticSymbol(declaration.getName(), groupSymbolRef);
            }
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(functionNode.name, declaration.getSymbolRef());

        BoundStatementNode statement;
        if (declaration.getReturnType() != SVoidType.instance && functionNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            statement = rewriteAsReturnStatement((ExpressionStatementNode) functionNode.body, declaration.getReturnType());
        } else {
            statement = bindStatement(functionNode.body);

            if (declaration.getReturnType() != SVoidType.instance && declaration.getReturnType() != SUnknown.instance) {
                if (new ControlFlowAnalyzer().analyzeStatement(statement) == FlowResult.CONTINUES) {
                    BlockStatementNode block = (BlockStatementNode) functionNode.body;
                    addDiagnostic(BinderErrors.NotAllPathReturnValue, block.closeBrace);
                }
            }
        }

        BoundFunctionDeclarationNode boundFunction = new BoundFunctionDeclarationNode(
                functionNode,
                declaration.getReturnTypeNode(),
                name,
                declaration.getParameters(),
                statement,
                context.getLifted());

        popScope();

        return boundFunction;
    }

    private BoundClassNode bindClass(ClassNode classNode) {
        ClassDeclaration declaration = declarationTable.getClassDeclaration(classNode);

        BoundNameExpressionNode name = new BoundNameExpressionNode(classNode.name, declaration.getSymbolRef());
        List<BoundClassMemberNode> members = new ArrayList<>();
        pushClassScope(declaration.getDeclaredType());

        for (ClassMemberNode member : classNode.members) {
            members.add(switch (member.getNodeType()) {
                case CLASS_FIELD -> bindClassField(declaration, (ClassFieldNode) member);
                case CLASS_CONSTRUCTOR -> bindClassConstructor(declaration, (ClassConstructorNode) member);
                case CLASS_METHOD -> bindClassMethod(declaration, (ClassMethodNode) member);
                case CLASS_OPERATOR_OVERLOAD -> bindClassOperatorOverload(declaration, (ClassOperatorOverloadNode) member);
                default -> throw new InternalException();
            });
        }

        // add default constructor if we have zero constructors defined
        ConstructorReference defaultBaseConstructor = null;
        if (members.stream().noneMatch(m -> m.getNodeType() == BoundNodeType.CLASS_CONSTRUCTOR)) {
            SType baseType = declaration.getDeclaredType().getBaseType();
            if (baseType != SUnknown.instance) {
                defaultBaseConstructor = baseType.getConstructors().stream().filter(c -> c.getParameters().isEmpty()).findFirst().orElse(null);
                if (defaultBaseConstructor == null) {
                    addDiagnostic(BinderErrors.BaseClassNoParameterlessConstructor, classNode.name);
                }
            }
        }

        popScope();

        return new BoundClassNode(classNode, name, declaration.getBaseTypeNode(), members, defaultBaseConstructor);
    }

    private BoundClassFieldNode bindClassField(ClassDeclaration classDeclaration, ClassFieldNode fieldNode) {
        ClassFieldDeclaration fieldDeclaration = classDeclaration.getFieldDeclaration(fieldNode);

        BoundNameExpressionNode name = new BoundNameExpressionNode(fieldNode.name, fieldDeclaration.getSymbolRef());
        return new BoundClassFieldNode(fieldNode, fieldDeclaration.getPropertyReference(), fieldDeclaration.getTypeNode(), name);
    }

    private BoundClassConstructorNode bindClassConstructor(ClassDeclaration classDeclaration, ClassConstructorNode constructorNode) {
        ClassConstructorDeclaration constructorDeclaration = classDeclaration.getConstructorDeclaration(constructorNode);

        pushConstructorScope();

        addParametersToContext(constructorDeclaration.getParameters());
        BoundConstructorInitializerNode initializer = bindConstructorInitializer(constructorNode);
        BoundStatementNode body = bindStatement(constructorNode.body);
        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        return new BoundClassConstructorNode(
                constructorNode,
                (SMethodFunction) constructorDeclaration.getSymbolRef().get().getType(),
                constructorDeclaration.getConstructorReference(),
                constructorDeclaration.getParameters(),
                initializer,
                body,
                lifted);
    }

    private BoundConstructorInitializerNode bindConstructorInitializer(ClassConstructorNode constructorNode) {
        if (constructorNode.initializer != null) {
            boolean isBaseCall;
            if (constructorNode.initializer.keyword.is(TokenType.THIS)) {
                isBaseCall = false;
            } else if (constructorNode.initializer.keyword.is(TokenType.BASE)) {
                isBaseCall = true;
            } else {
                throw new InternalException();
            }

            SType constructorOwner = isBaseCall ? context.getClassType().getBaseType() : context.getClassType();
            BindInvocableArgsResult<ConstructorReference> result = bindInvocableArguments(
                    constructorNode.initializer.arguments,
                    constructorOwner.getConstructors(),
                    UnknownConstructorReference.instance);

            if (result.noInvocables) {
                addDiagnostic(
                        BinderErrors.NoConstructors,
                        constructorNode.initializer,
                        constructorOwner.toString());
            } else if (result.noOverload) {
                addDiagnostic(
                        BinderErrors.NoOverloadedConstructors,
                        constructorNode.initializer,
                        constructorOwner.toString(), constructorNode.initializer.arguments.arguments.size());
            }

            return new BoundConstructorInitializerNode(
                    constructorNode.initializer,
                    result.argumentsListNode,
                    result.invocable);
        } else {
            SType baseType = context.getClassType().getBaseType();
            ConstructorReference constructor = baseType.getConstructors().stream()
                    .filter(c -> c.getParameters().isEmpty())
                    .findFirst()
                    .orElse(null);
            if (constructor != null) {
                return new BoundConstructorInitializerNode(
                        SyntaxFactory.missingConstructorInitializer(),
                        new BoundArgumentsListNode(SyntaxFactory.missingArgumentList(), List.of()),
                        constructor);
            } else {
                addDiagnostic(BinderErrors.BaseClassNoParameterlessConstructor, constructorNode.keyword);
                return new BoundConstructorInitializerNode(
                        SyntaxFactory.missingConstructorInitializer(),
                        new BoundArgumentsListNode(SyntaxFactory.missingArgumentList(), List.of()),
                        UnknownConstructorReference.instance);
            }
        }
    }

    private BoundClassMethodNode bindClassMethod(ClassDeclaration classDeclaration, ClassMethodNode methodNode) {
        ClassMethodDeclaration methodDeclaration = classDeclaration.getMethodDeclaration(methodNode);

        SType returnType = methodDeclaration.getTypeNode().type;

        pushMethodScope(returnType, methodDeclaration.isAsync());
        addParametersToContext(methodDeclaration.getParameters());

        BoundStatementNode body;
        if (returnType != SVoidType.instance && methodNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            body = rewriteAsReturnStatement((ExpressionStatementNode) methodNode.body, returnType);
        } else {
            body = bindStatement(methodNode.body);
        }

        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        if (returnType != SVoidType.instance && returnType != SUnknown.instance) {
            if (new ControlFlowAnalyzer().analyzeStatement(body) == FlowResult.CONTINUES) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(methodNode.name, methodDeclaration.getSymbolRef());
        return new BoundClassMethodNode(
                methodNode,
                (SMethodFunction) methodDeclaration.getSymbolRef().get().getType(),
                methodDeclaration.getMethodReference(),
                methodDeclaration.getTypeNode(),
                name,
                methodDeclaration.getParameters(),
                body,
                lifted);
    }

    private BoundClassOperatorOverloadNode bindClassOperatorOverload(ClassDeclaration classDeclaration, ClassOperatorOverloadNode operatorOverloadNode) {
        ClassUnaryOperationDeclaration unaryOperationDeclaration = classDeclaration.getUnaryOperationDeclaration(operatorOverloadNode);
        ClassBinaryOperationDeclaration binaryOperationDeclaration = classDeclaration.getBinaryOperationDeclaration(operatorOverloadNode);

        if (unaryOperationDeclaration == null && binaryOperationDeclaration == null) {
            throw new InternalException();
        }

        if (unaryOperationDeclaration != null) {
            return bindClassUnaryOperatorOverload(unaryOperationDeclaration, operatorOverloadNode);
        } else {
            return bindClassBinaryOperatorOverload(binaryOperationDeclaration, operatorOverloadNode);
        }
    }

    private BoundClassUnaryOperationNode bindClassUnaryOperatorOverload(ClassUnaryOperationDeclaration unaryOperationDeclaration, ClassOperatorOverloadNode operatorOverloadNode) {
        SType returnType = unaryOperationDeclaration.getReturnType();

        pushStaticMethodScope(returnType);
        addParametersToContext(unaryOperationDeclaration.getParameters());

        BoundStatementNode body;
        if (operatorOverloadNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            body = rewriteAsReturnStatement((ExpressionStatementNode) operatorOverloadNode.body, returnType);
        } else {
            body = bindStatement(operatorOverloadNode.body);
        }

        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        if (returnType != SUnknown.instance) {
            if (new ControlFlowAnalyzer().analyzeStatement(body) == FlowResult.CONTINUES) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        return new BoundClassUnaryOperationNode(
                operatorOverloadNode,
                (SMethodFunction) unaryOperationDeclaration.getSymbolRef().get().getType(),
                unaryOperationDeclaration.getMethodRef(),
                unaryOperationDeclaration.getOperator(),
                unaryOperationDeclaration.getReturnTypeNode(),
                unaryOperationDeclaration.getParameters(),
                body,
                lifted);
    }

    private BoundClassBinaryOperationNode bindClassBinaryOperatorOverload(ClassBinaryOperationDeclaration binaryOperationDeclaration, ClassOperatorOverloadNode operatorOverloadNode) {
        SType returnType = binaryOperationDeclaration.getReturnType();

        if (!binaryOperationDeclaration.getOperator().canBeOverloaded()) {
            addDiagnostic(BinderErrors.BinaryOperatorCannotBeOverloaded, operatorOverloadNode.operator, binaryOperationDeclaration.getOperator());
        } else {
            if (binaryOperationDeclaration.getOperator().isBooleanOnlyResult() && !returnType.equals(SBoolean.instance)) {
                addDiagnostic(BinderErrors.BinaryOperatorCanReturnBooleanOnly, operatorOverloadNode.returnType, binaryOperationDeclaration.getOperator());
            }
        }

        pushStaticMethodScope(returnType);
        addParametersToContext(binaryOperationDeclaration.getParameters());

        BoundStatementNode body;
        if (operatorOverloadNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            body = rewriteAsReturnStatement((ExpressionStatementNode) operatorOverloadNode.body, returnType);
        } else {
            body = bindStatement(operatorOverloadNode.body);
        }

        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        if (returnType != SUnknown.instance) {
            if (new ControlFlowAnalyzer().analyzeStatement(body) == FlowResult.CONTINUES) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        return new BoundClassBinaryOperationNode(
                operatorOverloadNode,
                (SMethodFunction) binaryOperationDeclaration.getSymbolRef().get().getType(),
                binaryOperationDeclaration.getMethodRef(),
                binaryOperationDeclaration.getOperator(),
                binaryOperationDeclaration.getReturnTypeNode(),
                binaryOperationDeclaration.getParameters(),
                body,
                lifted);
    }

    private BoundExtensionNode bindExtension(ExtensionNode extensionNode) {
        ExtensionDeclaration declaration = declarationTable.getExtensionDeclaration(extensionNode);

        List<BoundExtensionMemberNode> members = new ArrayList<>();
        pushExtensionScope(declaration.getBaseType());

        for (ClassMemberNode memberNode : extensionNode.members) {
            switch (memberNode.getNodeType()) {
                case CLASS_METHOD -> members.add(bindExtensionMethod(declaration, (ClassMethodNode) memberNode));
                case CLASS_OPERATOR_OVERLOAD -> members.add(bindExtensionOperationOverload(declaration, (ClassOperatorOverloadNode) memberNode));
                default -> throw new InternalException();
            }
        }

        popScope();
        return new BoundExtensionNode(extensionNode, declaration.getTypeNode(), members);
    }

    private BoundExtensionMethodNode bindExtensionMethod(ExtensionDeclaration declaration, ClassMethodNode methodNode) {
        ClassMethodDeclaration methodDeclaration = declaration.getMethodDeclaration(methodNode);

        SType returnType = methodDeclaration.getTypeNode().type;

        pushMethodScope(returnType, methodDeclaration.isAsync());
        addParametersToContext(methodDeclaration.getParameters());

        BoundStatementNode body;
        if (returnType != SVoidType.instance && methodNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            body = rewriteAsReturnStatement((ExpressionStatementNode) methodNode.body, returnType);
        } else {
            body = bindStatement(methodNode.body);
        }

        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        if (returnType != SVoidType.instance && returnType != SUnknown.instance) {
            if (new ControlFlowAnalyzer().analyzeStatement(body) == FlowResult.CONTINUES) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(methodNode.name, methodDeclaration.getSymbolRef());
        return new BoundExtensionMethodNode(
                methodNode,
                (SMethodFunction) methodDeclaration.getSymbolRef().get().getType(),
                methodDeclaration.getMethodReference(),
                methodDeclaration.getTypeNode(),
                name,
                methodDeclaration.getParameters(),
                body,
                lifted);
    }

    private BoundExtensionOperatorOverloadNode bindExtensionOperationOverload(ExtensionDeclaration declaration, ClassOperatorOverloadNode overloadNode) {
        ExtensionUnaryOperationDeclaration unaryOperationDeclaration = declaration.getUnaryOperationDeclaration(overloadNode);
        ExtensionBinaryOperationDeclaration binaryOperationDeclaration = declaration.getBinaryOperationDeclaration(overloadNode);

        if (unaryOperationDeclaration == null && binaryOperationDeclaration == null) {
            throw new InternalException();
        }

        if (unaryOperationDeclaration != null) {
            return bindExtensionUnaryOperatorOverload(unaryOperationDeclaration, overloadNode);
        } else {
            return bindExtensionBinaryOperatorOverload(binaryOperationDeclaration, overloadNode);
        }
    }

    private BoundExtensionUnaryOperationNode bindExtensionUnaryOperatorOverload(ExtensionUnaryOperationDeclaration unaryOperationDeclaration, ClassOperatorOverloadNode operatorOverloadNode) {
        SType returnType = unaryOperationDeclaration.getReturnType();

        pushStaticMethodScope(returnType);
        addParametersToContext(unaryOperationDeclaration.getParameters());

        BoundStatementNode body;
        if (operatorOverloadNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            body = rewriteAsReturnStatement((ExpressionStatementNode) operatorOverloadNode.body, returnType);
        } else {
            body = bindStatement(operatorOverloadNode.body);
        }

        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        if (returnType != SUnknown.instance) {
            if (new ControlFlowAnalyzer().analyzeStatement(body) == FlowResult.CONTINUES) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        return new BoundExtensionUnaryOperationNode(
                operatorOverloadNode,
                unaryOperationDeclaration.getOperation(),
                unaryOperationDeclaration.getReturnTypeNode(),
                unaryOperationDeclaration.getParameters(),
                body,
                lifted);
    }

    private BoundExtensionBinaryOperationNode bindExtensionBinaryOperatorOverload(ExtensionBinaryOperationDeclaration binaryOperationDeclaration, ClassOperatorOverloadNode operatorOverloadNode) {
        SType returnType = binaryOperationDeclaration.getReturnType();

        if (!binaryOperationDeclaration.getOperation().getOperator().canBeOverloaded()) {
            addDiagnostic(BinderErrors.BinaryOperatorCannotBeOverloaded, operatorOverloadNode.operator, binaryOperationDeclaration.getOperation().getOperator());
        } else {
            if (binaryOperationDeclaration.getOperation().getOperator().isBooleanOnlyResult() && !returnType.equals(SBoolean.instance)) {
                addDiagnostic(BinderErrors.BinaryOperatorCanReturnBooleanOnly, operatorOverloadNode.returnType, binaryOperationDeclaration.getOperation().getOperator());
            }
        }

        pushStaticMethodScope(returnType);
        addParametersToContext(binaryOperationDeclaration.getParameters());

        BoundStatementNode body;
        if (operatorOverloadNode.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            body = rewriteAsReturnStatement((ExpressionStatementNode) operatorOverloadNode.body, returnType);
        } else {
            body = bindStatement(operatorOverloadNode.body);
        }

        List<LiftedVariable> lifted = context.getLifted();

        popScope();

        if (returnType != SUnknown.instance) {
            if (new ControlFlowAnalyzer().analyzeStatement(body) == FlowResult.CONTINUES) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        return new BoundExtensionBinaryOperationNode(
                operatorOverloadNode,
                binaryOperationDeclaration.getOperation(),
                binaryOperationDeclaration.getReturnTypeNode(),
                binaryOperationDeclaration.getParameters(),
                body,
                lifted);
    }

    private BoundTypeAliasNode bindTypeAlias(TypeAliasNode typeAliasNode) {
        TypeAliasDeclaration declaration = declarationTable.getTypeAliasDeclaration(typeAliasNode);
        return declaration.getBound();
    }

    private BoundParameterListNode bindParameterList(ParameterListNode node) {
        List<BoundParameterNode> parameters = new ArrayList<>(node.parameters.size());
        for (ParameterNode parameter : node.parameters.getNodes()) {
            BoundTypeNode type = bindType(parameter.getType());
            BoundNameExpressionNode name = new BoundNameExpressionNode(parameter.getName(), new ForwardSymbolRef(), type.type);

            boolean duplicate = false;
            for (BoundParameterNode boundParameterNode : parameters) {
                if (name.value.isEmpty() || boundParameterNode.getName().value.isEmpty()) {
                    continue;
                }
                if (name.value.equals(boundParameterNode.getName().value)) {
                    duplicate = true;
                    break;
                }
            }

            if (duplicate) {
                addDiagnostic(BinderErrors.SymbolAlreadyDeclared, name, name.value);
            } else {
                parameters.add(new BoundParameterNode(name, type, parameter.getRange()));
            }
        }
        return new BoundParameterListNode(node, parameters);
    }

    private BoundStatementsListNode bindStatementList(StatementsListNode node) {
        List<BoundStatementNode> statements = node.statements.stream().map(this::bindStatement).toList();
        return new BoundStatementsListNode(node, statements, context.getLifted());
    }

    private BoundStatementNode bindStatement(StatementNode statement) {
        return switch (statement.getNodeType()) {
            case ASSIGNMENT_STATEMENT -> bindAssignmentStatement((AssignmentStatementNode) statement);
            case BLOCK_STATEMENT -> bindBlockStatement((BlockStatementNode) statement);
            case VARIABLE_DECLARATION -> bindVariableDeclaration((VariableDeclarationNode) statement);
            case EXPRESSION_STATEMENT -> bindExpressionStatement((ExpressionStatementNode) statement);
            case IF_STATEMENT -> bindIfStatement((IfStatementNode) statement);
            case RETURN_STATEMENT -> bindReturnStatement((ReturnStatementNode) statement);
            case FOR_LOOP_STATEMENT -> bindForLoopStatement((ForLoopStatementNode) statement);
            case FOREACH_LOOP_STATEMENT -> bindForEachLoopStatement((ForEachLoopStatementNode) statement);
            case WHILE_LOOP_STATEMENT -> bindWhileLoopStatement((WhileLoopStatementNode) statement);
            case BREAK_STATEMENT -> bindBreakStatement((BreakStatementNode) statement);
            case CONTINUE_STATEMENT -> bindContinueStatement((ContinueStatementNode) statement);
            case EMPTY_STATEMENT -> bindEmptyStatement((EmptyStatementNode) statement);
            case INVALID_STATEMENT -> bindInvalidStatement((InvalidStatementNode) statement);
            case INCREMENT_STATEMENT, DECREMENT_STATEMENT -> bindPostfixStatement((PostfixStatementNode) statement);
            case TRY_STATEMENT -> bindTryStatement((TryStatementNode) statement);
            case THROW_STATEMENT -> bindThrowStatement((ThrowStatementNode) statement);
            default -> throw new InternalException();
        };
    }

    private BoundBlockStatementNode bindBlockStatement(BlockStatementNode block) {
        pushScope();
        List<BoundStatementNode> statements = block.statements.stream().map(this::bindStatement).toList();
        popScope();
        return new BoundBlockStatementNode(block, statements);
    }

    private BoundStatementNode bindAssignmentStatement(AssignmentStatementNode statement) {
        BoundExpressionNode left = bindExpression(statement.left);
        if (!left.canSet()) {
            addDiagnostic(BinderErrors.ExpressionCannotBeSet, statement.left);
        }

        BoundAssignmentOperatorNode operatorNode = new BoundAssignmentOperatorNode(statement.operator);
        BinaryOperator operator = operatorNode.operator.getBinaryOperator();

        BoundExpressionNode right = operator != null && operator.isThrowOnTheRightSideAllowed() ?
                bindExpressionOrThrow(statement.right) :
                bindExpression(statement.right);

        if (operatorNode.operator == AssignmentOperator.ASSIGNMENT) {
            right = convert(right, left.type);
            return new BoundAssignmentStatementNode(statement, left, operatorNode, right);
        }

        assert operator != null;

        BinaryOperationResolveResult result = resolveBinaryOperation(left.type, operator, right.type, false);

        if (result == null) {
            addDiagnostic(
                    BinderErrors.BinaryOperatorNotDefined,
                    operatorNode,
                    operator,
                    left.type.toString(),
                    right.type.toString());
            return new BoundAugmentedAssignmentStatementNode(
                    statement,
                    left,
                    operatorNode,
                    UndefinedBinaryOperation.instance,
                    right);
        }

        if (result.rightCast != null) {
            right = new BoundImplicitCastExpressionNode(right, result.rightCast, right.getRange());
        }

        return new BoundAugmentedAssignmentStatementNode(
                statement,
                left,
                operatorNode,
                result.operation,
                right);
    }

    private BoundVariableDeclarationNode bindVariableDeclaration(VariableDeclarationNode variableDeclaration) {
        BoundTypeNode variableType;
        BoundExpressionNode expression;

        if (variableDeclaration.type.is(ParserNodeType.LET_TYPE)) {
            if (variableDeclaration.expression != null) {
                expression = bindExpression(variableDeclaration.expression);
                if (expression.getNodeType() == BoundNodeType.UNCONVERTED_LAMBDA) {
                    addDiagnostic(BinderErrors.LetUnboundLambda, variableDeclaration.type);
                }
                if (expression.getNodeType() == BoundNodeType.EMPTY_COLLECTION_EXPRESSION) {
                    addDiagnostic(BinderErrors.LetEmptyCollection, variableDeclaration.type);
                }
                if (expression.type == SNull.instance) {
                    addDiagnostic(BinderErrors.LetNull, variableDeclaration.type);
                }
            } else {
                TextRange range = variableDeclaration.name.getRange();
                expression = new BoundInvalidExpressionNode(List.of(), new SingleLineTextRange(range.getLine1(), range.getColumn1(), range.getPosition(), 0));
            }
            variableType = new BoundLetTypeNode((LetTypeNode) variableDeclaration.type, expression.type);
        } else {
            variableType = bindType(variableDeclaration.type);
            if (variableDeclaration.expression != null) {
                expression = convert(bindExpression(variableDeclaration.expression), variableType.type);
            } else {
                if (!variableType.type.hasDefaultValue()) {
                    addDiagnostic(BinderErrors.NoDefaultValue, variableDeclaration, variableType.type.toString());
                }
                expression = null;
            }
        }

        boolean exists = context.hasLocalSymbol(variableDeclaration.name.value);
        SymbolRef symbolRef;
        if (exists) {
            symbolRef = new InvalidSymbolRef();
            addDiagnostic(
                    BinderErrors.SymbolAlreadyDeclared,
                    variableDeclaration.name,
                    variableDeclaration.name.value);
        } else {
            LocalVariable variable = new LocalVariable(variableDeclaration.name.value, variableType.type, TextRange.combine(variableDeclaration.type, variableDeclaration.name));
            symbolRef = new MutableSymbolRef(variable);
            context.addLocalVariable(symbolRef);
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(variableDeclaration.name, symbolRef, variableType.type);

        return new BoundVariableDeclarationNode(variableDeclaration, variableType, name, expression);
    }

    private BoundExpressionStatementNode bindExpressionStatement(ExpressionStatementNode statement) {
        return new BoundExpressionStatementNode(statement, bindExpression(statement.expression));
    }

    private BoundIfStatementNode bindIfStatement(IfStatementNode statement) {
        ConditionFlow flow = bindExpressionAsConditionFlow(statement.condition);
        BoundExpressionNode condition = convert(flow.expression(), SBoolean.instance);

        pushScope();
        addVariablesToContext(flow.whenTrueLocals());
        BoundStatementNode thenStatement = bindStatement(statement.thenStatement);
        popScope();

        pushScope();
        addVariablesToContext(flow.whenFalseLocals());
        BoundStatementNode elseStatement = statement.elseStatement == null ? null : bindStatement(statement.elseStatement);
        popScope();

        boolean thenTerminates = new ControlFlowAnalyzer().analyzeStatement(thenStatement) == FlowResult.TERMINATES;
        boolean elseTerminates = elseStatement != null && new ControlFlowAnalyzer().analyzeStatement(elseStatement) == FlowResult.TERMINATES;

        List<SymbolRef> fallthroughLocals = List.of();
        if (thenTerminates && !elseTerminates && !flow.whenFalseLocals().isEmpty()) {
            fallthroughLocals = flow.whenFalseLocals();
        }
        if (!thenTerminates && elseTerminates && !flow.whenTrueLocals().isEmpty()) {
            fallthroughLocals = flow.whenTrueLocals();
        }

        addVariablesToContext(fallthroughLocals);

        return new BoundIfStatementNode(statement, condition, thenStatement, elseStatement, new FallthroughFlow(flow, fallthroughLocals));
    }

    private BoundReturnStatementNode bindReturnStatement(ReturnStatementNode statement) {
        if (statement.expression == null) {
            if (context.getReturnType() == SVoidType.instance) {
                return new BoundReturnStatementNode(statement, null);
            } else {
                addDiagnostic(
                        BinderErrors.EmptyReturnStatement,
                        statement);
                return new BoundReturnStatementNode(statement, new BoundInvalidExpressionNode(List.of(), statement.getRange().subRange(6)));
            }
        } else {
            BoundExpressionNode expression = bindExpression(statement.expression);
            SType expected = context.getReturnType();
            return new BoundReturnStatementNode(statement, convert(expression, expected));
        }
    }

    private BoundForLoopStatementNode bindForLoopStatement(ForLoopStatementNode statement) {
        pushScope(new LoopFrame(context.getFrame(), new Label(), new Label()));
        BoundStatementNode init = statement.init != null ? bindStatement(statement.init) : null;

        BoundExpressionNode condition;
        if (statement.condition != null) {
            condition = convert(bindExpression(statement.condition), SBoolean.instance);
            if (condition.type != SBoolean.instance) {
                addDiagnostic(BinderErrors.CannotImplicitlyConvert, condition, condition.type.toString(), SBoolean.instance.toString());
            }
        } else {
            condition = null;
        }

        BoundStatementNode update = statement.update != null ? bindStatement(statement.update) : null;
        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundForLoopStatementNode(statement, init, condition, update, body);
    }

    private BoundForEachLoopStatementNode bindForEachLoopStatement(ForEachLoopStatementNode statement) {
        pushScope(new LoopFrame(context.getFrame(), new Label(), new Label()));

        SymbolRef index = context.addLocalVariable(null, SInt.instance, null);
        SymbolRef length = context.addLocalVariable(null, SInt.instance, null);

        BoundTypeNode variableType;

        BoundExpressionNode iterable = bindExpression(statement.iterable);
        if (iterable.type instanceof SArrayType arrayType) {
            if (statement.typeNode.is(ParserNodeType.LET_TYPE)) {
                variableType = new BoundLetTypeNode((LetTypeNode) statement.typeNode, arrayType.getElementsType());
            } else {
                variableType = bindType(statement.typeNode);
                if (!arrayType.getElementsType().equals(variableType.type)) {
                    addDiagnostic(BinderErrors.ForEachTypesNotMatch, statement.typeNode);
                }
            }
        } else {
            addDiagnostic(BinderErrors.CannotIterate, iterable, iterable.type.toString());

            if (statement.typeNode.is(ParserNodeType.LET_TYPE)) {
                variableType = new BoundLetTypeNode((LetTypeNode) statement.typeNode, SUnknown.instance);
            } else {
                variableType = bindType(statement.typeNode);
            }
        }

        SymbolRef existingRef = context.getSymbol(statement.name.value);
        BoundNameExpressionNode name;
        if (existingRef != null) {
            name = new BoundNameExpressionNode(new InvalidSymbolRef());
            addDiagnostic(
                    BinderErrors.SymbolAlreadyDeclared,
                    statement.name,
                    statement.name.value);
        } else {
            SymbolRef symbolRef = context.addLocalVariable(statement.name.value, variableType.type, TextRange.combine(statement.typeNode, statement.name));
            name = new BoundNameExpressionNode(statement.name, symbolRef);
        }

        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundForEachLoopStatementNode(
                statement,
                variableType, name, iterable, body,
                index, length);
    }

    private BoundWhileLoopStatementNode bindWhileLoopStatement(WhileLoopStatementNode statement) {
        pushScope(new LoopFrame(context.getFrame(), new Label(), new Label()));

        BoundExpressionNode condition = convert(bindExpression(statement.condition), SBoolean.instance);
        if (condition.type != SBoolean.instance) {
            addDiagnostic(BinderErrors.CannotImplicitlyConvert, condition, condition.type.toString(), SBoolean.instance.toString());
        }

        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundWhileLoopStatementNode(condition, body, statement);
    }

    private BoundBreakStatementNode bindBreakStatement(BreakStatementNode statement) {
        boolean isInsideLoop = context.getFrame().getClosestLoop() != null;
        if (!isInsideLoop) {
            addDiagnostic(BinderErrors.NoLoop, statement);
        }
        return new BoundBreakStatementNode(statement, isInsideLoop);
    }

    private BoundContinueStatementNode bindContinueStatement(ContinueStatementNode statement) {
        boolean isInsideLoop = context.getFrame().getClosestLoop() != null;
        if (!isInsideLoop) {
            addDiagnostic(BinderErrors.NoLoop, statement);
        }
        return new BoundContinueStatementNode(statement, isInsideLoop);
    }

    private BoundEmptyStatementNode bindEmptyStatement(EmptyStatementNode statement) {
        return new BoundEmptyStatementNode(statement);
    }

    private BoundInvalidStatementNode bindInvalidStatement(InvalidStatementNode statement) {
        return new BoundInvalidStatementNode(statement);
    }

    private BoundPostfixStatementNode bindPostfixStatement(PostfixStatementNode statement) {
        BoundExpressionNode expression = bindExpression(statement.expression);
        if (!expression.canSet()) {
            addDiagnostic(BinderErrors.ExpressionCannotBeSet, expression);
        }

        boolean isInc = statement.is(ParserNodeType.INCREMENT_STATEMENT);

        SType type = expression.type;
        PostfixOperation operation = isInc ? type.increment() : type.decrement();
        if (operation == null) {
            operation = UndefinedPostfixOperation.INSTANCE;
            addDiagnostic(
                    BinderErrors.CannotApplyIncDec,
                    statement,
                    isInc ? "++" : "--",
                    type.toString());
        }

        return new BoundPostfixStatementNode(
                statement,
                isInc ? BoundNodeType.INCREMENT_STATEMENT : BoundNodeType.DECREMENT_STATEMENT,
                expression,
                operation);
    }

    private BoundTryStatementNode bindTryStatement(TryStatementNode statement) {
        BoundBlockStatementNode block = bindBlockStatement(statement.block);

        BoundBlockStatementNode catchBlock = null;
        BoundSymbolNode exceptionSymbol = null;
        if (statement.catchClause != null) {
            pushScope();
            if (statement.catchClause.declaration != null) {
                String name = statement.catchClause.declaration.identifier.value;
                boolean exists = context.hasLocalSymbol(name);
                SymbolRef symbolRef;
                if (exists) {
                    symbolRef = new InvalidSymbolRef();
                    addDiagnostic(
                            BinderErrors.SymbolAlreadyDeclared,
                            statement.catchClause.declaration.identifier,
                            name);
                } else {
                    LocalVariable variable = new LocalVariable(name, SType.fromJavaType(Throwable.class), statement.catchClause.declaration.identifier.getRange());
                    symbolRef = new MutableSymbolRef(variable);
                    context.addLocalVariable(symbolRef);
                }

                exceptionSymbol = new BoundSymbolNode(statement.catchClause.declaration.identifier, symbolRef);
            }

            pushScope(new TryCatchFrame(context.getFrame(), new LocalVariable(SType.fromJavaType(Throwable.class))));
            catchBlock = bindBlockStatement(statement.catchClause.block);
            popScope();

            popScope();
        }

        BoundBlockStatementNode finallyBlock = null;
        if (statement.finallyClause != null) {
            finallyBlock = bindBlockStatement(statement.finallyClause.block);
        }

        return new BoundTryStatementNode(
                statement,
                block,
                exceptionSymbol,
                catchBlock,
                finallyBlock);
    }

    private BoundThrowStatementNode bindThrowStatement(ThrowStatementNode statement) {
        if (statement.expression != null) {
            BoundExpressionNode expression = bindExpression(statement.expression);
            SType throwable = SType.fromJavaType(Throwable.class);
            if (!expression.type.isInstanceOf(throwable)) {
                addDiagnostic(BinderErrors.ThrowInvalidType, expression, throwable, expression.type);
            }

            return new BoundThrowStatementNode(statement, expression);
        } else {
            TryCatchFrame tryCatchFrame = context.getFrame().getClosestTryCatch();
            if (tryCatchFrame == null) {
                addDiagnostic(BinderErrors.RethrowNotAllowed, statement);
            }

            return new BoundThrowStatementNode(statement, null);
        }
    }

    private BoundExpressionNode bindExpression(ExpressionNode expression) {
        BoundExpressionNode result = bindExpressionAll(expression);

        if (result.is(BoundNodeType.STATIC_REFERENCE)) {
            addDiagnostic(BinderErrors.TypeReferenceNotAllowed, expression, expression.getRange().extract(code));
            return new BoundInvalidExpressionNode(List.of(result), List.of(), expression.getRange());
        }

        if (result.is(BoundNodeType.THROW_EXPRESSION)) {
            addDiagnostic(BinderErrors.ThrowExpressionNotAllowed, expression, expression.getRange().extract(code));
            return new BoundInvalidExpressionNode(List.of(result), List.of(), expression.getRange());
        }

        return result;
    }

    private BoundExpressionNode bindExpressionOrStaticRef(ExpressionNode expression) {
        BoundExpressionNode result = bindExpressionAll(expression);

        if (result.is(BoundNodeType.THROW_EXPRESSION)) {
            addDiagnostic(BinderErrors.ThrowExpressionNotAllowed, expression, expression.getRange().extract(code));
            return new BoundInvalidExpressionNode(List.of(result), List.of(), expression.getRange());
        }

        return result;
    }

    private BoundExpressionNode bindExpressionOrThrow(ExpressionNode expression) {
        BoundExpressionNode result = bindExpressionAll(expression);

        if (result.is(BoundNodeType.STATIC_REFERENCE)) {
            addDiagnostic(BinderErrors.TypeReferenceNotAllowed, expression, expression.getRange().extract(code));
            return new BoundInvalidExpressionNode(List.of(result), List.of(), expression.getRange());
        }

        return result;
    }

    private BoundExpressionNode bindExpressionAll(ExpressionNode expression) {
        return switch (expression.getNodeType()) {
            case NULL_EXPRESSION -> bindNullExpression((NullExpressionNode) expression);
            case BOOLEAN_LITERAL -> bindBooleanLiteralExpression((BooleanLiteralExpressionNode) expression);
            case INTEGER_LITERAL -> bindIntegerLiteralExpression((IntegerLiteralExpressionNode) expression);
            case INTEGER64_LITERAL -> bindInteger64LiteralExpression((Integer64LiteralExpressionNode) expression);
            case FLOAT_LITERAL -> bindFloatLiteralExpression((FloatLiteralExpressionNode) expression);
            case STRING_LITERAL -> bindStringLiteralExpression((StringLiteralExpressionNode) expression);
            case CHAR_LITERAL -> bindCharLiteralExpression((CharLiteralExpressionNode) expression);
            case PARENTHESIZED_EXPRESSION -> bindParenthesizedExpression((ParenthesizedExpressionNode) expression);
            case UNARY_EXPRESSION -> bindUnaryExpression((UnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> bindBinaryExpression((BinaryExpressionNode) expression);
            case IS_EXPRESSION -> bindIsExpression((IsExpressionNode) expression);
            case TYPE_CAST_EXPRESSION -> bindTypeCastExpression((TypeCastExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> bindConditionalExpression((ConditionalExpressionNode) expression);
            case INDEX_EXPRESSION -> bindIndexExpression((IndexExpressionNode) expression);
            case INVOCATION_EXPRESSION -> bindInvocationExpression((InvocationExpressionNode) expression);
            case NAME_EXPRESSION -> bindNameExpression((NameExpressionNode) expression);
            case THIS_EXPRESSION -> bindThisExpression((ThisExpressionNode) expression);
            case BASE_EXPRESSION -> bindBaseExpression((BaseExpressionNode) expression);
            case STATIC_REFERENCE -> bindStaticReferenceExpression((StaticReferenceNode) expression);
            case MEMBER_ACCESS_EXPRESSION -> bindMemberAccessExpression((MemberAccessExpressionNode) expression);
            case REF_ARGUMENT_EXPRESSION -> bindRefArgumentExpression((RefArgumentExpressionNode) expression);
            case ARRAY_CREATION_EXPRESSION -> bindArrayCreationExpression((ArrayCreationExpressionNode) expression);
            case ARRAY_INITIALIZER_EXPRESSION -> bindArrayInitializerExpression((ArrayInitializerExpressionNode) expression);
            case OBJECT_CREATION_EXPRESSION -> bindObjectCreationExpressionNode((ObjectCreationExpressionNode) expression);
            case COLLECTION_EXPRESSION -> bindCollectionExpression((CollectionExpressionNode) expression);
            case LAMBDA_EXPRESSION -> bindLambdaExpression((LambdaExpressionNode) expression);
            case AWAIT_EXPRESSION -> bindAwaitExpression((AwaitExpressionNode) expression);
            case META_INVALID_EXPRESSION -> bindInvalidMetaExpression((InvalidMetaExpressionNode) expression);
            case META_CAST_EXPRESSION -> bindMetaCastExpression((MetaCastExpressionNode) expression);
            case META_TYPE_EXPRESSION -> bindMetaTypeExpression((MetaTypeExpressionNode) expression);
            case META_TYPE_OF_EXPRESSION -> bindMetaTypeOfExpression((MetaTypeOfExpressionNode) expression);
            case THROW_EXPRESSION -> bindThrowExpression((ThrowExpressionNode) expression);
            case INVALID_EXPRESSION -> bindInvalidExpression((InvalidExpressionNode) expression);
            default -> throw new InternalException();
        };
    }

    private ConditionFlow bindExpressionAsConditionFlow(ExpressionNode expression) {
        return switch (expression.getNodeType()) {
            case PARENTHESIZED_EXPRESSION -> bindParenthesizedExpressionAsCondition((ParenthesizedExpressionNode) expression);
            case IS_EXPRESSION -> bindIsExpressionAsCondition((IsExpressionNode) expression);
            case UNARY_EXPRESSION -> bindUnaryExpressionAsCondition((UnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> bindBinaryExpressionAsCondition((BinaryExpressionNode) expression);
            default -> new ConditionFlow(bindExpression(expression));
        };
    }

    private BoundParenthesizedExpressionNode bindParenthesizedExpression(ParenthesizedExpressionNode parenthesizedExpression) {
        return new BoundParenthesizedExpressionNode(parenthesizedExpression, bindExpression(parenthesizedExpression.inner));
    }

    private BoundExpressionNode bindUnaryExpression(UnaryExpressionNode unary) {
        if (unary.operand.is(ParserNodeType.INTEGER_LITERAL)) {
            IntegerLiteralExpressionNode literal = (IntegerLiteralExpressionNode) unary.operand;
            return bindIntegerLiteralExpression(literal.withSign(unary.operator));
        }
        if (unary.operand.is(ParserNodeType.INTEGER64_LITERAL)) {
            Integer64LiteralExpressionNode literal = (Integer64LiteralExpressionNode) unary.operand;
            return bindInteger64LiteralExpression(literal.withSign(unary.operator));
        }

        BoundExpressionNode operand = bindExpression(unary.operand);
        UnaryOperation operation = resolveUnaryOperation(unary.operator.operator, operand.type);
        BoundUnaryOperatorNode operator;
        if (operation != null) {
            operator = new BoundUnaryOperatorNode(unary.operator, operation);
        } else {
            addDiagnostic(
                    BinderErrors.UnaryOperatorNotDefined,
                    unary,
                    unary.operator.operator.toString(),
                    operand.type.toString());
            operator = new BoundUnaryOperatorNode(unary.operator, UndefinedUnaryOperation.instance);
        }
        return new BoundUnaryExpressionNode(unary, operator, operand);
    }

    private BoundExpressionNode bindBinaryExpression(BinaryExpressionNode binary) {
        if (binary.operator.token.is(TokenType.IN)) {
            return bindInExpression(binary);
        }

        BoundExpressionNode left = bindExpression(binary.left);
        BoundExpressionNode right = binary.operator.operator.isThrowOnTheRightSideAllowed() ?
                bindExpressionOrThrow(binary.right) :
                bindExpression(binary.right);

        BinaryOperationResolveResult result = resolveBinaryOperation(left.type, binary.operator.operator, right.type, true);
        if (result == null) {
            addDiagnostic(
                    BinderErrors.BinaryOperatorNotDefined,
                    binary,
                    binary.operator.operator.toString(),
                    left.type.toString(),
                    right.type.toString());
            BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(binary.operator, UndefinedBinaryOperation.instance);
            return new BoundBinaryExpressionNode(binary, left, operator, right);
        }

        if (result.leftCast != null) {
            left = new BoundImplicitCastExpressionNode(left, result.leftCast, left.getRange());
        }
        if (result.rightCast != null) {
            right = new BoundImplicitCastExpressionNode(right, result.rightCast, right.getRange());
        }

        BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(binary.operator, result.operation);
        return new BoundBinaryExpressionNode(binary, left, operator, right);
    }

    private BoundIsExpressionNode bindIsExpression(IsExpressionNode is) {
        BoundExpressionNode expression = bindExpression(is.expression);
        BoundPatternNode pattern = bindPattern(is.pattern).pattern;
        return new BoundIsExpressionNode(is, expression, pattern);
    }

    private ConditionFlow bindParenthesizedExpressionAsCondition(ParenthesizedExpressionNode parenthesized) {
        ConditionFlow flow = bindExpressionAsConditionFlow(parenthesized.inner);
        return new ConditionFlow(
                new BoundParenthesizedExpressionNode(parenthesized, flow.expression()),
                flow.whenTrueLocals(),
                flow.whenFalseLocals(),
                flow.allLocals());
    }

    private ConditionFlow bindIsExpressionAsCondition(IsExpressionNode is) {
        BoundExpressionNode expression = bindExpression(is.expression);
        PatternFlow flow = bindPattern(is.pattern);
        return new ConditionFlow(
                new BoundIsExpressionNode(is, expression, flow.pattern),
                flow.whenTrueLocals,
                flow.whenFalseLocals,
                Stream.concat(flow.whenTrueLocals.stream(), flow.whenFalseLocals.stream()).toList());
    }

    private ConditionFlow bindUnaryExpressionAsCondition(UnaryExpressionNode unary) {
        UnaryOperator operator = unary.operator.operator;
        if (operator == UnaryOperator.NOT) {
            ConditionFlow inner = bindExpressionAsConditionFlow(unary.operand);
            if (inner.expression().type == SBoolean.instance) {
                UnaryOperation operation = SBoolean.NOT.value();
                BoundUnaryOperatorNode boundOperator = new BoundUnaryOperatorNode(unary.operator, operation);
                return new ConditionFlow(
                        new BoundUnaryExpressionNode(unary, boundOperator, inner.expression()),
                        inner.whenFalseLocals(),
                        inner.whenTrueLocals(),
                        inner.allLocals());
            }
        }

        // TODO: double binding when condition above fails?

        return new ConditionFlow(bindUnaryExpression(unary));
    }

    private ConditionFlow bindBinaryExpressionAsCondition(BinaryExpressionNode binary) {
        BiFunction<List<SymbolRef>, List<SymbolRef>, List<SymbolRef>> merge = (list1, list2) -> {
            if (list2.isEmpty()) {
                return list1;
            }
            if (list1.isEmpty()) {
                return list2;
            }

            List<SymbolRef> result = new ArrayList<>(list1);
            for (SymbolRef ref : list2) {
                if (result.stream().anyMatch(r -> r.get().getName().equals(ref.get().getName()))) {
                    addDiagnostic(
                            BinderErrors.VariableRedeclarationInCondition,
                            binary,
                            ref.get().getName());
                } else {
                    result.add(ref);
                }
            }
            return result;
        };

        BinaryOperator operator = binary.operator.operator;
        if (operator == BinaryOperator.BOOLEAN_AND || operator == BinaryOperator.BOOLEAN_OR) {
            ConditionFlow left = bindExpressionAsConditionFlow(binary.left);
            ConditionFlow right = bindExpressionAsConditionFlow(binary.right);
            if (left.expression().type == SBoolean.instance && right.expression().type == SBoolean.instance) {
                BinaryOperation operation = operator == BinaryOperator.BOOLEAN_AND ? SBoolean.BOOLEAN_AND.value() : SBoolean.BOOLEAN_OR.value();
                if (operation == null) {
                    throw new InternalException();
                }
                BoundBinaryOperatorNode boundOperator = new BoundBinaryOperatorNode(binary.operator, operation);

                List<SymbolRef> whenTrueLocals;
                if (operator == BinaryOperator.BOOLEAN_AND) {
                    whenTrueLocals = merge.apply(left.whenTrueLocals(), right.whenTrueLocals());
                } else {
                    whenTrueLocals = List.of();
                }

                List<SymbolRef> whenFalseLocals;
                if (operator == BinaryOperator.BOOLEAN_OR) {
                    whenFalseLocals = merge.apply(left.whenFalseLocals(), right.whenFalseLocals());
                } else {
                    whenFalseLocals = List.of();
                }

                return new ConditionFlow(
                        new BoundBinaryExpressionNode(binary, left.expression(), boundOperator, right.expression()),
                        whenTrueLocals,
                        whenFalseLocals,
                        Stream.concat(left.allLocals().stream(), right.allLocals().stream()).toList());
            }
        }

        // TODO: double binding when condition above fails?

        return new ConditionFlow(bindBinaryExpression(binary));
    }

    private PatternFlow bindPattern(PatternNode pattern) {
        return switch (pattern.getNodeType()) {
            case NOT_PATTERN -> {
                NotPatternNode notPatternNode = (NotPatternNode) pattern;
                PatternFlow inner = bindPattern(notPatternNode.inner);
                yield new PatternFlow(
                        new BoundNotPattern(notPatternNode, inner.pattern, notPatternNode.getRange()),
                        inner.whenFalseLocals,
                        inner.whenTrueLocals);
            }
            case CONSTANT_PATTERN -> {
                ConstantPatternNode constantPatternNode = (ConstantPatternNode) pattern;
                BoundExpressionNode expression = bindExpression(constantPatternNode.expression);
                if (!isConstant(expression)) {
                    addDiagnostic(BinderErrors.ConstantExpressionExpected, expression);
                }
                yield new PatternFlow(
                        new BoundConstantPatternNode(constantPatternNode, expression, constantPatternNode.getRange()));
            }
            case TYPE_PATTERN -> {
                TypePatternNode typePatternNode = (TypePatternNode) pattern;
                BoundTypeNode typeNode = bindType(typePatternNode.typeNode);
                yield new PatternFlow(
                        new BoundTypePatternNode(typePatternNode, typeNode, typePatternNode.getRange()));
            }
            case DECLARATION_PATTERN -> {
                DeclarationPatternNode declarationPatternNode = (DeclarationPatternNode) pattern;
                BoundTypeNode typeNode = bindType(declarationPatternNode.typeNode);
                String name = declarationPatternNode.identifier.value;

                SymbolRef existingRef = context.getSymbol(name);
                SymbolRef symbolRef;
                List<SymbolRef> whenTrueLocals;
                if (existingRef != null) {
                    symbolRef = new InvalidSymbolRef();
                    whenTrueLocals = List.of();
                    addDiagnostic(
                            BinderErrors.SymbolAlreadyDeclared,
                            declarationPatternNode.identifier,
                            name);
                } else {
                    LocalVariable variable = new LocalVariable(name, typeNode.type, pattern.getRange());
                    symbolRef = new MutableSymbolRef(variable);
                    whenTrueLocals = List.of(symbolRef);
                }

                BoundSymbolNode symbolNode = new BoundSymbolNode(declarationPatternNode.identifier, symbolRef);
                yield new PatternFlow(
                        new BoundDeclarationPatternNode(declarationPatternNode, typeNode, symbolNode, declarationPatternNode.getRange()),
                        whenTrueLocals,
                        List.of());
            }
            default -> throw new InternalException();
        };
    }

    private boolean isConstant(BoundExpressionNode expression) {
        return switch (expression.getNodeType()) {
            case NULL_EXPRESSION, BOOLEAN_LITERAL, CHAR_LITERAL, INTEGER_LITERAL, INTEGER64_LITERAL, FLOAT_LITERAL, STRING_LITERAL -> true;
            default -> false;
        };
    }

    private BoundTypeCastExpressionNode bindTypeCastExpression(TypeCastExpressionNode test) {
        BoundExpressionNode expression = bindExpression(test.expression);
        BoundTypeNode type = bindType(test.type);
        return new BoundTypeCastExpressionNode(test, expression, type);
    }

    private BoundExpressionNode bindInExpression(BinaryExpressionNode binary) {
        BoundExpressionNode left = bindExpression(binary.left);
        BoundExpressionNode right = bindExpression(binary.right);

        if (left.type == SUnknown.instance || right.type == SUnknown.instance) {
            return new BoundInExpressionNode(binary, left, right, UnknownMethodReference.instance);
        }

        List<MethodReference> methods = getInstanceMethodsWithExtensions(right.type, false);
        for (MethodReference method : methods) {
            if (!method.getName().equals("contains")) {
                continue;
            }
            if (method.getReturn() != SBoolean.instance) {
                continue;
            }
            List<SType> parameters = method.getParameterTypes();
            if (parameters.size() != 1) {
                continue;
            }
            SType parameter = parameters.getFirst();
            if (parameter.equals(left.type)) {
                return new BoundInExpressionNode(binary, left, right, method);
            }
        }

        // TODO: check with casts as second priority

        addDiagnostic(
                BinderErrors.CannotUseInOperator,
                binary,
                right.type.toString(),
                left.type.toString());
        return new BoundInExpressionNode(binary, left, right, UnknownMethodReference.instance);
    }

    private BoundNullExpressionNode bindNullExpression(NullExpressionNode expression) {
        return new BoundNullExpressionNode(expression);
    }

    private BoundBooleanLiteralExpressionNode bindBooleanLiteralExpression(BooleanLiteralExpressionNode bool) {
        return new BoundBooleanLiteralExpressionNode(bool);
    }

    private BoundIntegerLiteralExpressionNode bindIntegerLiteralExpression(IntegerLiteralExpressionNode literal) {
        int value = 0;
        if (literal.value.startsWith("0x")) {
            if (literal.value.length() > 8 + 2) {
                addDiagnostic(BinderErrors.IntegerConstantTooLarge, literal);
                return new BoundIntegerLiteralExpressionNode(literal, 0);
            }
            for (int i = 2; i < literal.value.length(); i++) {
                value = (value << 4) | parseHex(literal.value.charAt(i));
            }
        } else {
            try {
                value = Integer.parseInt(literal.value);
            } catch (NumberFormatException e) {
                ErrorCode code = literal.value.charAt(0) == '-' ? BinderErrors.IntegerConstantTooSmall : BinderErrors.IntegerConstantTooLarge;
                addDiagnostic(code, literal);
            }
        }

        return new BoundIntegerLiteralExpressionNode(literal, value);
    }

    private BoundInteger64LiteralExpressionNode bindInteger64LiteralExpression(Integer64LiteralExpressionNode literal) {
        String str = literal.value.substring(0, literal.value.length() - 1); // remove L

        long value = 0;
        if (str.startsWith("0x")) {
            if (str.length() > 16 + 2) {
                addDiagnostic(BinderErrors.IntegerConstantTooLarge, literal);
                return new BoundInteger64LiteralExpressionNode(literal, 0);
            }
            for (int i = 2; i < str.length(); i++) {
                value = (value << 4) | parseHex(str.charAt(i));
            }
        } else {
            try {
                value = Long.parseLong(str);
            } catch (NumberFormatException e) {
                ErrorCode code = str.charAt(0) == '-' ? BinderErrors.IntegerConstantTooSmall : BinderErrors.IntegerConstantTooLarge;
                addDiagnostic(code, literal);
            }
        }

        return new BoundInteger64LiteralExpressionNode(literal, value);
    }

    private BoundFloatLiteralExpressionNode bindFloatLiteralExpression(FloatLiteralExpressionNode literal) {
        double value;
        try {
            value = Double.parseDouble(literal.value);
        } catch (NumberFormatException e) {
            value = 0;
            addDiagnostic(BinderErrors.InvalidFloatConstant, literal);
        }
        return new BoundFloatLiteralExpressionNode(literal, value);
    }

    private BoundStringLiteralExpressionNode bindStringLiteralExpression(StringLiteralExpressionNode literal) {
        return new BoundStringLiteralExpressionNode(literal, literal.value);
    }

    private BoundCharLiteralExpressionNode bindCharLiteralExpression(CharLiteralExpressionNode literal) {
        return new BoundCharLiteralExpressionNode(literal);
    }

    private BoundConditionalExpressionNode bindConditionalExpression(ConditionalExpressionNode expression) {
        BoundExpressionNode condition = convert(bindExpression(expression.condition), SBoolean.instance);
        BoundExpressionNode whenTrue = bindExpressionOrThrow(expression.whenTrue);
        BoundExpressionNode whenFalse = bindExpressionOrThrow(expression.whenFalse);

        ExpressionPair pair = tryCastToCommon(whenTrue, whenFalse);
        if (!pair.result) {
            addDiagnostic(
                    BinderErrors.CannotDetermineConditionalExpressionType,
                    expression,
                    whenTrue.type.toString(), whenFalse.type.toString());
        }

        return new BoundConditionalExpressionNode(expression, condition, pair.expression1, pair.expression2);
    }

    private BoundExpressionNode bindInvocationExpression(InvocationExpressionNode invocation) {
        if (invocation.callee instanceof MemberAccessExpressionNode memberAccessNode && memberAccessNode.callee.is(ParserNodeType.BASE_EXPRESSION)) {
            return bindBaseMethodCall(invocation);
        }

        BoundExpressionNode callee = bindExpression(invocation.callee);

        if (callee.getNodeType() == BoundNodeType.FUNCTION_GROUP) {
            BoundFunctionGroupExpressionNode functionGroup = (BoundFunctionGroupExpressionNode) callee;
            BindInvocableArgsResult<Function> result = bindInvocableArguments(
                    invocation.arguments,
                    functionGroup.candidates,
                    UnknownFunction.instance);
            if (result.noInvocables) {
                // there should always be candidates
                throw new InternalException();
            } else if (result.noOverload) {
                if (functionGroup.candidates.size() == 1) {
                    // if there is only 1 candidate, bind it, and output more user-friendly error
                    result = new BindInvocableArgsResult<>(
                            functionGroup.candidates.getFirst(),
                            result.argumentsListNode(),
                            false, false);
                    addDiagnostic(
                            BinderErrors.ArgumentCountMismatch,
                            functionGroup,
                            functionGroup.syntaxNode.value,
                            result.invocable.getParameters().size());
                } else {
                    addDiagnostic(
                            BinderErrors.FunctionDoesNotExist,
                            functionGroup,
                            functionGroup.syntaxNode.value,
                            invocation.arguments.arguments.size());
                }
            }

            BoundFunctionNode functionNode = new BoundFunctionNode(functionGroup.syntaxNode, result.invocable);
            return new BoundFunctionInvocationExpression(
                    invocation,
                    functionNode,
                    result.invocable.getFunctionType().getReturnType(),
                    result.argumentsListNode,
                    context.releaseRefVariables(),
                    invocation.getRange());
        }

        if (callee.getNodeType() == BoundNodeType.METHOD_GROUP) {
            BoundMethodGroupExpressionNode methodGroup = (BoundMethodGroupExpressionNode) callee;
            BindInvocableArgsResult<MethodReference> result = bindInvocableArguments(
                    invocation.arguments,
                    methodGroup.candidates,
                    UnknownMethodReference.instance);
            if (result.noInvocables) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        methodGroup.method,
                        methodGroup.callee.type, methodGroup.method.name);
            } else if (result.noOverload) {
                addDiagnostic(
                        BinderErrors.NoOverloadedMethods,
                        methodGroup.method,
                        methodGroup.method.name, invocation.arguments.arguments.size());
            }

            verifyMethodAccessible(result.invocable, methodGroup.syntaxNode.name);

            BoundMethodNode methodNode = new BoundMethodNode(methodGroup.syntaxNode.name, result.invocable);
            return new BoundMethodInvocationExpressionNode(
                    invocation,
                    methodGroup.callee,
                    methodNode,
                    result.argumentsListNode,
                    context.releaseRefVariables());
        }

        if (callee.type instanceof SFunction function) {
            InvocableObject invocable = new InvocableObject(function);
            BindInvocableArgsResult<InvocableObject> result = bindInvocableArguments(
                    invocation.arguments,
                    List.of(invocable));

            if (result.noOverload) {
                addDiagnostic(
                        BinderErrors.ArgumentCountMismatch2,
                        invocation.arguments,
                        function.getParameters().size());
            }

            return new BoundObjectInvocationExpression(callee, result.argumentsListNode, invocation.getRange());
        }

        if (callee.type != SUnknown.instance) {
            addDiagnostic(BinderErrors.NotFunction, callee);
        }

        return new BoundInvalidExpressionNode(List.of(callee), List.of(invocation.arguments), invocation.getRange());
    }

    private BoundExpressionNode bindBaseMethodCall(InvocationExpressionNode invocation) {
        MemberAccessExpressionNode memberAccessNode = (MemberAccessExpressionNode) invocation.callee;
        BaseExpressionNode baseNode = (BaseExpressionNode) memberAccessNode.callee;

        if (!context.isClassMethod() || !context.isDeclaredClass()) {
            addDiagnostic(BinderErrors.BaseInvalidContext, baseNode);
            // TODO: add parameters
            return new BoundInvalidExpressionNode(List.of(), List.of(invocation), invocation.getRange());
        }

        String methodName = memberAccessNode.name.value;
        SType baseType = context.getClassType().getBaseType();

        // intentionally skip extension methods
        List<MethodReference> candidates = baseType.getInstanceMethods().stream()
                .filter(m -> m.getName().equals(methodName))
                .toList();
        if (candidates.isEmpty()) {
            addDiagnostic(
                    BinderErrors.MemberDoesNotExist,
                    memberAccessNode.name,
                    baseType.toString(), methodName);
            // TODO: add parameters
            return new BoundInvalidExpressionNode(List.of(), List.of(invocation), invocation.getRange());
        }

        BindInvocableArgsResult<MethodReference> result = bindInvocableArguments(
                invocation.arguments,
                candidates,
                UnknownMethodReference.instance);
        if (result.noInvocables) {
            addDiagnostic(
                    BinderErrors.MemberDoesNotExist,
                    memberAccessNode.name,
                    baseType.toString(), methodName);
        } else if (result.noOverload) {
            addDiagnostic(
                    BinderErrors.NoOverloadedMethods,
                    memberAccessNode,
                    methodName, invocation.arguments.arguments.size());
        }

        BoundMethodNode methodNode = new BoundMethodNode(memberAccessNode.name, result.invocable);
        return new BoundBaseMethodInvocationExpressionNode(
                invocation,
                methodNode,
                result.argumentsListNode,
                context.releaseRefVariables());
    }

    private BoundExpressionNode bindUnconvertedLambda(BoundUnconvertedLambdaExpressionNode node, SFunction target) {
        if (target instanceof SFunctionalInterface functionalInterface) {
            return bindLambdaExpression(
                    node.lambda,
                    target,
                    functionalInterface.getRawReturnType(),
                    functionalInterface.getActualReturnType(),
                    functionalInterface.getRawParameters(),
                    functionalInterface.getActualParameters());
        } else {
            return bindLambdaExpression(
                    node.lambda,
                    target,
                    target.getReturnType(),
                    target.getReturnType(),
                    target.getParameterTypes().toArray(new SType[0]),
                    target.getParameterTypes().toArray(new SType[0]));
        }
    }

    private BoundExpressionNode bindLambdaExpression(
            LambdaExpressionNode node,
            SFunction functionType,
            SType rawReturnType,
            SType actualReturnType,
            SType[] rawParameterTypes,
            SType[] actualParameterTypes
    ) {
        int parametersCount = actualParameterTypes.length;
        if (node.parameters.size() != parametersCount) {
            throw new InternalException("Lambda parameters count mismatch.");
        }

        pushFunctionScope(actualReturnType, false);

        for (int i = 0; i < parametersCount; i++) {
            context.addLocalParameter2(null, rawParameterTypes[i], null);
        }

        List<BoundParameterNode> parameters = new ArrayList<>();
        for (int i = 0; i < parametersCount; i++) {
            NameExpressionNode name = node.parameters.getNodeAt(i);
            SType type = actualParameterTypes[i];
            SymbolRef symbolRef = context.addLocalParameter2(name.value, type, node.parameters.getNodeAt(i).getRange());
            TextRange range = name.getRange();
            BoundNameExpressionNode boundName = new BoundNameExpressionNode(name, symbolRef);
            parameters.add(new BoundParameterNode(boundName, type, range));
        }

        BoundStatementNode statement;
        if (functionType.isFunction() && node.body.is(ParserNodeType.EXPRESSION_STATEMENT)) {
            statement = rewriteAsReturnStatement((ExpressionStatementNode) node.body, actualReturnType);
        } else {
            statement = bindStatement(node.body);
        }

        List<LiftedVariable> lifted = context.getLifted();
        List<CapturedVariable> captured = context.getCaptured();

        popScope();

        return new BoundLambdaExpressionNode(
                node,
                parameters,
                statement,
                functionType,
                lifted,
                captured);
    }

    private BoundExpressionNode bindNameExpression(NameExpressionNode name) {
        SymbolRef symbolRef = getSymbol(name.value);

        if (symbolRef != null) {
            if (symbolRef.get() instanceof FunctionGroup group) {
                return new BoundFunctionGroupExpressionNode(name, group.getFunctions());
            } else if (symbolRef.get() instanceof TypeAliasSymbol typeAliasSymbol) {
                CustomTypeNode custom = new CustomTypeNode(name.token);
                BoundAliasedTypeNode typeNode = new BoundAliasedTypeNode(custom, symbolRef);
                return new BoundStaticReferenceExpression(name, typeNode, new SStaticTypeReference(typeNode.type));
            } else {
                return new BoundNameExpressionNode(name, symbolRef);
            }
        }

        if (context.isClassMethod() && !context.isExtension()) {
            PropertyReference property = context.getClassType().getInstanceProperties().stream()
                    .filter(p -> p.getName().equals(name.value))
                    .findFirst()
                    .orElse(null);
            if (property != null) {
                TextRange ephemeralRange = name.getRange().getStart();
                ThisExpressionNode ephemeralThis = new ThisExpressionNode(new Token(TokenType.THIS, ephemeralRange));
                return new BoundPropertyAccessExpressionNode(
                        new MemberAccessExpressionNode(
                                ephemeralThis,
                                new Token(TokenType.DOT, ephemeralRange),
                                name),
                        new BoundThisExpressionNode(
                                ephemeralThis,
                                context.getClassType(),
                                name.getRange().getStart()),
                        new BoundPropertyNode(name, property),
                        name.getRange());
            }

            List<MethodReference> methods = getInstanceMethodsWithExtensions(context.getClassType(), false).stream()
                    .filter(m -> m.getName().equals(name.value))
                    .toList();
            if (!methods.isEmpty()) {
                return new BoundMethodGroupExpressionNode(
                        new MemberAccessExpressionNode(
                                new ThisExpressionNode(new Token(TokenType.THIS, name.getRange().getStart())),
                                new Token(TokenType.DOT, name.getRange().getStart()),
                                name),
                        new BoundThisExpressionNode(
                                new ThisExpressionNode(new Token(TokenType.THIS, name.getRange().getStart())),
                                context.getClassType(),
                                name.getRange().getStart()),
                        methods,
                        new BoundUnresolvedMethodNode(name));
            }
        }

        Optional<Class<?>> optional = parameters.getCustomTypes().stream().filter(c -> {
            return c.getAnnotation(CustomType.class).name().equals(name.value);
        }).findFirst();
        if (optional.isPresent()) {
            SType type = SType.fromJavaType(optional.get());
            CustomTypeNode custom = new CustomTypeNode(name.token);
            BoundCustomTypeNode typeNode = new BoundCustomTypeNode(custom, type, name.getRange());
            return new BoundStaticReferenceExpression(name, typeNode, new SStaticTypeReference(type));
        }

        addDiagnostic(
                BinderErrors.NameDoesNotExist,
                name,
                name.value);
        return new BoundNameExpressionNode(name, new InvalidSymbolRef(), SUnknown.instance);
    }

    private BoundThisExpressionNode bindThisExpression(ThisExpressionNode expression) {
        if (context.isClassMethod()) {
            if (context.isDeclaredClass()) {
                return new BoundThisExpressionNode(expression, context.getClassType());
            }
            if (context.isExtension()) {
                return new BoundThisExpressionNode(expression, context.getExtensionType());
            }
            throw new InternalException();
        } else {
            addDiagnostic(BinderErrors.ThisInvalidContext, expression);
            return new BoundThisExpressionNode(expression, SUnknown.instance);
        }
    }

    private BoundExpressionNode bindBaseExpression(BaseExpressionNode expression) {
        addDiagnostic(BinderErrors.BaseInvalidUse, expression);
        return new BoundInvalidExpressionNode(List.of(), List.of(expression), expression.getRange());
    }

    private BoundStaticReferenceExpression bindStaticReferenceExpression(StaticReferenceNode node) {
        BoundTypeNode typeNode = bindType(node.typeNode);
        return new BoundStaticReferenceExpression(node, typeNode, new SStaticTypeReference(typeNode.type));
    }

    private BoundArrayCreationExpressionNode bindArrayCreationExpression(ArrayCreationExpressionNode expression) {
        BoundTypeNode typeNode = bindType(expression.typeNode);
        BoundExpressionNode lengthExpression = convert(bindExpression(expression.lengthExpression), SInt.instance);
        return new BoundArrayCreationExpressionNode(expression, typeNode, lengthExpression);
    }

    private BoundArrayInitializerExpressionNode bindArrayInitializerExpression(ArrayInitializerExpressionNode expression) {
        if (expression.typeNode.isNot(ParserNodeType.ARRAY_TYPE)) {
            throw new InternalException();
        }

        BoundTypeNode typeNode = bindType(expression.typeNode);
        SArrayType type = (SArrayType) typeNode.type;
        List<BoundExpressionNode> items = new ArrayList<>(expression.list.size());
        for (ExpressionNode e : expression.list.getNodes()) {
            items.add(convert(bindExpression(e), type.getElementsType()));
        }

        return new BoundArrayInitializerExpressionNode(expression, typeNode, items);
    }

    private BoundObjectCreationExpressionNode bindObjectCreationExpressionNode(ObjectCreationExpressionNode expression) {
        BoundTypeNode typeNode = bindType(expression.typeNode);

        if (typeNode.type.isAbstract()) {
            addDiagnostic(
                    BinderErrors.CannotInstantiateAbstractClass,
                    expression);
        }

        BindInvocableArgsResult<ConstructorReference> result = bindInvocableArguments(
                expression.arguments,
                typeNode.type.getConstructors(),
                UnknownConstructorReference.instance);

        if (result.noOverload) {
            addDiagnostic(
                    BinderErrors.NoOverloadedConstructors,
                    expression,
                    typeNode.type.toString(), expression.arguments.arguments.size());
        }

        return new BoundObjectCreationExpressionNode(
                expression,
                typeNode,
                result.invocable,
                result.argumentsListNode);
    }

    private <T extends Invocable> BindInvocableArgsResult<T> bindInvocableArguments(ArgumentsListNode argumentsListNode, List<T> candidates) {
        if (candidates.size() != 1) {
            throw new InternalException();
        }
        return bindInvocableArguments(argumentsListNode, candidates, null);
    }

    private <T extends Invocable> BindInvocableArgsResult<T> bindInvocableArguments(ArgumentsListNode argumentsListNode, List<T> candidates, @Nullable T unknown) {
        int argumentsSize = argumentsListNode.arguments.size();
        List<BoundExpressionNode> arguments = new ArrayList<>(argumentsSize);
        for (ExpressionNode node : argumentsListNode.arguments.getNodes()) {
            arguments.add(bindExpression(node));
        }

        T matchedInvocable = unknown == null ? candidates.getFirst() : unknown;
        boolean noInvocables = candidates.isEmpty();
        boolean noOverloads = false;

        List<T> invocables = candidates.stream()
                .filter(c -> c == unknown || c.getParameters().size() == argumentsSize)
                .toList();
        if (invocables.isEmpty()) {
            noOverloads = true;
        } else {
            List<ArgumentsCast<T>> possibleArgumentsWithCasting = invocables
                    .stream()
                    .map(invocable -> {
                        if (invocable == unknown) {
                            return new ArgumentsCast<>(unknown, Collections.nCopies(argumentsSize, new ConversionInfo(ConversionType.IDENTITY)), 0);
                        }

                        List<SType> parameterTypes = invocable.getParameterTypes();
                        List<ConversionInfo> conversions = new ArrayList<>();
                        int count = 0;
                        for (int i = 0; i < parameterTypes.size(); i++) {
                            SType expected = parameterTypes.get(i);
                            BoundExpressionNode argument = arguments.get(i);
                            ConversionInfo conversion = getConversionInfo(argument, expected);
                            if (conversion != null) {
                                conversions.add(conversion);
                                if (conversion.type() == ConversionType.IMPLICIT_CAST) {
                                    count++;
                                }
                            } else {
                                return null;
                            }
                        }
                        return new ArgumentsCast<>(invocable, conversions, count);
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(ac -> ac.count))
                    .toList();

            if (possibleArgumentsWithCasting.isEmpty()) {
                addDiagnostic(BinderErrors.CannotCastArguments, argumentsListNode);
            } else {
                ArgumentsCast<T> overload = possibleArgumentsWithCasting.getFirst();
                for (int i = 0; i < argumentsSize; i++) {
                    BoundExpressionNode argument = arguments.get(i);
                    ConversionInfo conversion = overload.conversions.get(i);
                    SType parameterType = overload.invocable != unknown ? overload.invocable.getParameterTypes().get(i) : SUnknown.instance;
                    if (conversion.type() != ConversionType.IDENTITY) {
                        arguments.set(i, convert(conversion, argument, parameterType));
                    }
                }

                matchedInvocable = overload.invocable;
            }
        }

        BoundArgumentsListNode boundArgumentsListNode = new BoundArgumentsListNode(argumentsListNode, arguments);
        return new BindInvocableArgsResult<>(matchedInvocable, boundArgumentsListNode, noInvocables, noOverloads);
    }

    private @Nullable UnaryOperation resolveUnaryOperation(UnaryOperator operator, SType type) {
        for (UnaryOperation operation : declarationTable.appendExtensionUnaryOperations(type, type.getUnaryOperations())) {
            if (operation.getOperator() != operator) {
                continue;
            }
            if (operation.getOperandType().equals(type)) {
                return operation;
            }
        }

        return null;
    }

    private @Nullable BinaryOperationResolveResult resolveBinaryOperation(SType left, BinaryOperator operator, SType right, boolean allowLeftCast) {
        BinaryOperation operation = resolveDirectBinaryOperation(left, operator, right);
        if (operation != null) {
            return new BinaryOperationResolveResult(operation);
        }

        if (!left.equals(right)) {
            // try implicit cast right to left
            CastOperation cast = SType.implicitCastTo(right, left);
            if (cast != null) {
                operation = resolveDirectBinaryOperation(left, operator, left);
                if (operation != null) {
                    return new BinaryOperationResolveResult(null, operation, cast);
                }
            }

            // try implicit cast left to right
            if (allowLeftCast) {
                cast = SType.implicitCastTo(left, right);
                if (cast != null) {
                    operation = resolveDirectBinaryOperation(right, operator, right);
                    if (operation != null) {
                        return new BinaryOperationResolveResult(cast, operation, null);
                    }
                }
            }

            // try all implicit casts on the left
            if (allowLeftCast) {
                for (CastOperation cast1 : left.getImplicitCasts()) {
                    operation = resolveDirectBinaryOperation(cast1.getDstType(), operator, right);
                    if (operation != null) {
                        return new BinaryOperationResolveResult(cast1, operation, null);
                    }
                }
            }

            // try all implicit casts on the right
            for (CastOperation cast1 : right.getImplicitCasts()) {
                operation = resolveDirectBinaryOperation(left, operator, cast1.getDstType());
                if (operation != null) {
                    return new BinaryOperationResolveResult(null, operation, cast1);
                }
            }
        }

        return null;
    }

    private @Nullable BinaryOperation resolveDirectBinaryOperation(SType left, BinaryOperator operator, SType right) {
        if (left == SUnknown.instance || right == SUnknown.instance) {
            return UndefinedBinaryOperation.instance;
        }

        for (BinaryOperation operation : left.getBinaryOperations()) {
            if (operation.getOperator() == operator && operation.getLeft().isCompatibleWith(left) && operation.getRight().isCompatibleWith(right)) {
                return operation;
            }
        }

        for (BinaryOperation operation : right.getBinaryOperations()) {
            if (operation.getOperator() == operator && operation.getLeft().isCompatibleWith(left) && operation.getRight().isCompatibleWith(right)) {
                return operation;
            }
        }

        List<BinaryOperation> extensionOperations = declarationTable.getExtensionBinaryOperations();

        for (BinaryOperation operation : extensionOperations) {
            if (operation.getOperator() == operator && operation.getLeft().isCompatibleWith(left) && operation.getRight().isCompatibleWith(right)) {
                return operation;
            }
        }

        for (BinaryOperation operation : extensionOperations) {
            if (operation.getOperator() == operator && operation.getLeft().isCompatibleWith(left) && operation.getRight().isCompatibleWith(right)) {
                return operation;
            }
        }

        return null;
    }

    private BoundExpressionNode bindCollectionExpression(CollectionExpressionNode collection) {
        if (collection.list.size() == 0) {
            return new BoundEmptyCollectionExpressionNode(collection);
        }

        List<BoundExpressionNode> items = collection.list.getNodes().stream().map(this::bindExpression).toList();
        SType type = items.getFirst().type;
        for (int i = 1; i < items.size(); i++) {
            if (!items.get(i).type.equals(type)) {
                addDiagnostic(BinderErrors.CannotInferCollectionExpressionTypes, collection.list.getNodeAt(i), type, i, items.get(i).type);
                break;
            }
        }

        return new BoundCollectionExpressionNode(collection, new SArrayType(type), items);
    }

    private BoundExpressionNode bindMemberAccessExpression(MemberAccessExpressionNode expression) {
        BoundExpressionNode callee = bindExpressionOrStaticRef(expression.callee);

        if (callee.type == SNull.instance) {
            addDiagnostic(BinderErrors.CannotAccessNullMembers, expression.operator);
            return new BoundPropertyAccessExpressionNode(
                    expression,
                    callee,
                    new BoundPropertyNode(expression.name, UnknownPropertyReference.instance));
        }

        boolean isPrivate = expression.isPrivate();

        if (callee.type instanceof SStaticTypeReference staticType) {
            PropertyReference property = staticType.getUnderlying().getStaticProperties().stream()
                    .filter(p -> p.isPublic() ^ isPrivate)
                    .filter(p -> p.getName().equals(expression.name.value))
                    .findFirst()
                    .orElse(null);
            if (property != null) {
                verifyPropertyAccessible(property, expression.name);
                return new BoundPropertyAccessExpressionNode(
                        expression,
                        callee,
                        new BoundPropertyNode(expression.name, property));
            }

            List<MethodReference> methods = staticType.getUnderlying().getStaticMethods().stream()
                    .filter(p -> p.isPublic() ^ isPrivate)
                    .filter(m -> m.getName().equals(expression.name.value))
                    .filter(m -> {
                        if (m instanceof NativeMethodReference ref) {
                            return context.isMethodVisible(ref.getUnderlying());
                        } else {
                            return true;
                        }
                    })
                    .toList();
            if (methods.isEmpty()) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        expression.name,
                        callee.type.toString(), expression.name.value);
                return new BoundPropertyAccessExpressionNode(
                        expression,
                        callee,
                        new BoundPropertyNode(expression.name, UnknownPropertyReference.instance));
            }

            return new BoundMethodGroupExpressionNode(
                    expression,
                    callee,
                    methods,
                    new BoundUnresolvedMethodNode(expression.name));
        } else {
            PropertyReference property;
            if (callee.type == SUnknown.instance || expression.name.value.isEmpty()) {
                property = UnknownPropertyReference.instance;
            } else {
                property = callee.type.getInstanceProperties().stream()
                        .filter(p -> p.isPublic() ^ isPrivate)
                        .filter(p -> p.getName().equals(expression.name.value))
                        .findFirst()
                        .orElse(null);
            }
            if (property != null) {
                verifyPropertyAccessible(property, expression.name);
                return new BoundPropertyAccessExpressionNode(
                        expression,
                        callee,
                        new BoundPropertyNode(expression.name, property));
            }

            List<MethodReference> methods = getInstanceMethodsWithExtensions(callee.type, isPrivate)
                    .stream()
                    .filter(m -> m.getName().equals(expression.name.value))
                    .filter(m -> {
                        if (m instanceof NativeMethodReference ref) {
                            return context.isMethodVisible(ref.getUnderlying());
                        } else {
                            return true;
                        }
                    })
                    .toList();
            if (methods.isEmpty()) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        expression.name,
                        callee.type.toString(), expression.name.value);
                return new BoundPropertyAccessExpressionNode(
                        expression,
                        callee,
                        new BoundPropertyNode(expression.name, UnknownPropertyReference.instance));
            }

            return new BoundMethodGroupExpressionNode(
                    expression,
                    callee,
                    methods,
                    new BoundUnresolvedMethodNode(expression.name));
        }
    }

    private void verifyPropertyAccessible(PropertyReference propertyRef, Locatable locatable) {
        if (propertyRef.isPublic()) {
            return;
        }

        if (propertyRef instanceof FieldPropertyReference fieldPropertyRef) {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                MethodHandles.privateLookupIn(fieldPropertyRef.getUnderlyingField().getDeclaringClass(), lookup);
            } catch (IllegalAccessException | SecurityException e) {
                addDiagnostic(BinderErrors.PrivateAccessDenied, locatable, e.toString());
            }
        }
    }

    private void verifyMethodAccessible(MethodReference methodRef, Locatable locatable) {
        if (methodRef instanceof NativeMethodReference nativeMethodRef) {
            MethodUsagePolicy policy = parameters.getMethodUsagePolicy();
            if (policy != null) {
                policy.validate(nativeMethodRef.getUnderlying())
                        .ifPresent(message -> addDiagnostic(BinderErrors.MethodUsageNotAllowed, locatable, message));
            }
        }

        if (!methodRef.isPublic()) {
            if (methodRef instanceof NativeMethodReference nativeMethodRef) {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                try {
                    MethodHandles.privateLookupIn(nativeMethodRef.getUnderlying().getDeclaringClass(), lookup);
                } catch (IllegalAccessException | SecurityException e) {
                    addDiagnostic(BinderErrors.PrivateAccessDenied, locatable, e.toString());
                }
            }
        }
    }

    private List<MethodReference> getInstanceMethodsWithExtensions(SType type, boolean isPrivate) {
        List<MethodReference> methods = new ArrayList<>();
        for (MethodReference method : type.getInstanceMethods()) {
            if (method.isPublic() ^ isPrivate) {
                methods.add(method);
            }
        }
        if (!isPrivate) {
            declarationTable.appendExtensionMethods(type, methods);
        }
        return methods;
    }

    private BoundRefArgumentExpressionNode bindRefArgumentExpression(RefArgumentExpressionNode expression) {
        BoundExpressionNode boundExpressionNode = bindNameExpression(expression.name);
        if (boundExpressionNode.getNodeType() != BoundNodeType.NAME_EXPRESSION) {
            addDiagnostic(BinderErrors.InvalidRefExpression, expression);
            return createInvalidRefArgumentExpression(
                    expression,
                    new BoundNameExpressionNode(expression.name, new InvalidSymbolRef()));
        }

        BoundNameExpressionNode name = (BoundNameExpressionNode) boundExpressionNode;

        if (boundExpressionNode.type == SUnknown.instance) {
            return createInvalidRefArgumentExpression(expression, name);
        } else {
            SByReference type = boundExpressionNode.type.getReferenceType();
            if (type == null) {
                addDiagnostic(BinderErrors.RefTypeNotSupported, expression, boundExpressionNode.type);
                return createInvalidRefArgumentExpression(expression, name);
            }
            Variable variable = name.symbolRef.asVariable();
            LocalVariable holder = context.createRefVariable(variable);
            return new BoundRefArgumentExpressionNode(expression, name, holder, type);
        }
    }

    private BoundRefArgumentExpressionNode createInvalidRefArgumentExpression(RefArgumentExpressionNode expression, BoundNameExpressionNode name) {
        return new BoundRefArgumentExpressionNode(
                expression,
                name,
                new LocalVariable("", SUnknown.instance, TextRange.MISSING),
                SUnknown.instance);
    }

    private BoundIndexExpressionNode bindIndexExpression(IndexExpressionNode indexExpression) {
        BoundExpressionNode callee = bindExpression(indexExpression.callee);
        BoundExpressionNode index = bindExpression(indexExpression.index);

        IndexOperation operation = null;
        List<IndexOperation> operations = callee.type.getIndexOperations();
        for (IndexOperation o : operations) {
            if (o.indexType.equals(index.type)) {
                operation = o;
                break;
            }
        }

        if (operation == null) {
            for (IndexOperation o : operations) {
                CastOperation cast = SType.implicitCastTo(index.type, o.indexType);
                if (cast != null) {
                    operation = o;
                    index = new BoundImplicitCastExpressionNode(index, cast, index.getRange());
                    break;
                }
            }
        }

        // TODO: canGet/canSet

        if (operation == null) {
            operation = UndefinedIndexOperation.instance;
            addDiagnostic(
                    BinderErrors.CannotApplyIndex,
                    indexExpression,
                    callee.type.toString());
        }

        return new BoundIndexExpressionNode(indexExpression, callee, index, operation);
    }

    private BoundExpressionNode bindLambdaExpression(LambdaExpressionNode expression) {
        LambdaAnalyzer analyzer = new LambdaAnalyzer();
        boolean canBeAction = analyzer.canBeAction(expression);
        boolean canBeFunction = analyzer.canBeFunction(expression);
        return new BoundUnconvertedLambdaExpressionNode(
                expression,
                new SUnconvertedLambda(canBeAction, canBeFunction, expression.parameters.size()),
                expression.getRange());
    }

    private BoundAwaitExpressionNode bindAwaitExpression(AwaitExpressionNode node) {
        if (!context.isAsync()) {
            addDiagnostic(BinderErrors.AwaitInNonAsyncContext, node.keyword);
        }

        BoundExpressionNode expression = bindExpression(node.expression);
        if (expression.type == SUnknown.instance) {
            return new BoundAwaitExpressionNode(node, expression, SUnknown.instance);
        }
        if (expression.type instanceof SFuture future) {
            return new BoundAwaitExpressionNode(node, expression, future.getUnderlying());
        } else {
            addDiagnostic(BinderErrors.CannotAwaitNonFuture, expression);
            return new BoundAwaitExpressionNode(node, expression, SUnknown.instance);
        }
    }

    private BoundInvalidMetaExpressionNode bindInvalidMetaExpression(InvalidMetaExpressionNode expression) {
        return new BoundInvalidMetaExpressionNode(expression);
    }

    private BoundMetaCastExpressionNode bindMetaCastExpression(MetaCastExpressionNode meta) {
        BoundExpressionNode expression = bindExpression(meta.expression);
        BoundTypeNode type = bindType(meta.type);
        return new BoundMetaCastExpressionNode(meta, expression, type);
    }

    private BoundMetaTypeExpressionNode bindMetaTypeExpression(MetaTypeExpressionNode meta) {
        BoundTypeNode type = bindType(meta.type);
        return new BoundMetaTypeExpressionNode(meta, type);
    }

    private BoundMetaTypeOfExpressionNode bindMetaTypeOfExpression(MetaTypeOfExpressionNode meta) {
        BoundExpressionNode expression = bindExpression(meta.expression);
        return new BoundMetaTypeOfExpressionNode(meta, expression);
    }

    private BoundThrowExpressionNode bindThrowExpression(ThrowExpressionNode throwExpression) {
        BoundExpressionNode expression = bindExpression(throwExpression.expression);
        SType throwable = SType.fromJavaType(Throwable.class);
        if (!expression.type.isInstanceOf(throwable)) {
            addDiagnostic(BinderErrors.ThrowInvalidType, expression, throwable, expression.type);
        }

        return new BoundThrowExpressionNode(throwExpression, expression);
    }

    private BoundInvalidExpressionNode bindInvalidExpression(InvalidExpressionNode expression) {
        return new BoundInvalidExpressionNode(expression, List.of(), List.of(), expression.getRange());
    }

    private BoundExpressionNode convert(BoundExpressionNode expression, SType type) {
        ConversionInfo info = getConversionInfo(expression, type);
        if (info == null) {
            addDiagnostic(
                    BinderErrors.CannotImplicitlyConvert,
                    expression,
                    expression.type.toString(), type.toString());
            return expression;
        }

        return convert(info, expression, type);
    }

    private BoundExpressionNode convert(ConversionInfo info, BoundExpressionNode expression, SType type) {
        if (info.type() == ConversionType.IDENTITY) {
            return expression;
        }

        if (info.type() == ConversionType.EMPTY_ARRAY) {
            return new BoundCollectionExpressionNode((BoundEmptyCollectionExpressionNode) expression, type);
        }

        if (info.type() == ConversionType.LAMBDA_BINDING) {
            return bindUnconvertedLambda((BoundUnconvertedLambdaExpressionNode) expression, (SFunction) type);
        }

        return new BoundConversionNode(expression, info, type, expression.getRange());
    }

    @Nullable
    private ConversionInfo getConversionInfo(BoundExpressionNode expression, SType type) {
        if (expression.type.isInstanceOf(type)) {
            return new ConversionInfo(ConversionType.IDENTITY);
        }

        CastOperation operation = SType.implicitCastTo(expression.type, type);
        if (operation != null) {
            return new ConversionInfo(ConversionType.IMPLICIT_CAST, operation);
        }

        if (expression.getNodeType() == BoundNodeType.EMPTY_COLLECTION_EXPRESSION) {
            if (type instanceof SArrayType) {
                return new ConversionInfo(ConversionType.EMPTY_ARRAY);
            } else {
                return null;
            }
        }

        if (expression.getNodeType() == BoundNodeType.UNCONVERTED_LAMBDA) {
            SUnconvertedLambda lambdaType = (SUnconvertedLambda) expression.type;
            if (type instanceof SFunction function) {
                if (function.getReturnType() == SVoidType.instance && !lambdaType.canBeAction()) {
                    return null;
                }
                if (function.getReturnType() != SVoidType.instance && !lambdaType.canBeFunction()) {
                    return null;
                }
                if (function.getParameters().size() != lambdaType.getParametersCount()) {
                    return null;
                }
                return new ConversionInfo(ConversionType.LAMBDA_BINDING);
            }
            return null;
        }

        if (expression.getNodeType() == BoundNodeType.FUNCTION_GROUP) {
            BoundFunctionGroupExpressionNode functionGroupExpressionNode = (BoundFunctionGroupExpressionNode) expression;
            if (type instanceof SFunctionalInterface functionalInterface) {
                for (Function candidate : functionGroupExpressionNode.candidates) {
                    if (candidate.getFunctionType().signatureMatchesWithBoxing(functionalInterface)) {
                        return new ConversionInfo(ConversionType.FUNCTION_TO_INTERFACE, candidate);
                    }
                }
            }
            if (type instanceof SGenericFunction genericFunction) {
                for (Function candidate : functionGroupExpressionNode.candidates) {
                    if (candidate.getFunctionType().signatureMatchesWithBoxing(genericFunction)) {
                        return new ConversionInfo(ConversionType.FUNCTION_TO_GENERIC, candidate);
                    }
                }
            }
            return null;
        }

        if (expression.getNodeType() == BoundNodeType.METHOD_GROUP) {
            BoundMethodGroupExpressionNode methodGroupExpressionNode = (BoundMethodGroupExpressionNode) expression;
            if (type instanceof SFunctionalInterface functionalInterface) {
                for (MethodReference method : methodGroupExpressionNode.candidates) {
                    if (method.signatureMatchesWithBoxing(functionalInterface)) {
                        return new ConversionInfo(ConversionType.METHOD_GROUP_TO_INTERFACE, method);
                    }
                }
                return null;
            }
            if (type instanceof SGenericFunction genericFunction) {
                for (MethodReference method : methodGroupExpressionNode.candidates) {
                    if (method.signatureMatchesWithBoxing(genericFunction)) {
                        return new ConversionInfo(ConversionType.METHOD_GROUP_TO_GENERIC, method);
                    }
                }
                return null;
            }
            return null;
        }

        return null;
    }

    private ExpressionPair tryCastToCommon(BoundExpressionNode expression1, BoundExpressionNode expression2) {
        if (expression1.type.equals(expression2.type)) {
            return new ExpressionPair(expression1, expression2);
        }

        CastOperation operation1 = SType.implicitCastTo(expression1.type, expression2.type);
        if (operation1 != null) {
            expression1 = new BoundImplicitCastExpressionNode(expression1, operation1, expression1.getRange());
            return new ExpressionPair(expression1, expression2);
        }

        CastOperation operation2 = SType.implicitCastTo(expression2.type, expression1.type);
        if (operation2 != null) {
            expression2 = new BoundImplicitCastExpressionNode(expression2, operation2, expression2.getRange());
            return new ExpressionPair(expression1, expression2);
        }

        expression1 = new BoundImplicitCastExpressionNode(expression1, UndefinedCastOperation.instance, expression1.getRange());
        expression2 = new BoundImplicitCastExpressionNode(expression2, UndefinedCastOperation.instance, expression2.getRange());
        return new ExpressionPair(false, expression1, expression2);
    }

    private BoundTypeNode bindType(TypeNode type) {
        return switch (type.getNodeType()) {
            case INVALID_TYPE -> {
                InvalidTypeNode invalid = (InvalidTypeNode) type;
                yield new BoundInvalidTypeNode(invalid);
            }

            case VOID_TYPE -> {
                VoidTypeNode voidTypeNode = (VoidTypeNode) type;
                yield new BoundVoidTypeNode(voidTypeNode);
            }

            case PREDEFINED_TYPE -> {
                PredefinedTypeNode predefined = (PredefinedTypeNode) type;
                yield new BoundPredefinedTypeNode(predefined, switch (predefined.type) {
                    case BOOLEAN -> SBoolean.instance;
                    case INT8 -> SInt8.instance;
                    case INT16 -> SInt16.instance;
                    case INT -> SInt.instance;
                    case INT64 -> SInt64.instance;
                    case FLOAT32 -> SFloat32.instance;
                    case FLOAT -> SFloat.instance;
                    case STRING -> SString.instance;
                    case CHAR -> SChar.instance;
                });
            }

            case CUSTOM_TYPE -> {
                CustomTypeNode custom = (CustomTypeNode) type;
                SymbolRef symbolRef = getSymbol(custom.value);
                if (symbolRef != null) {
                    if (symbolRef.get() instanceof ClassSymbol) {
                        yield new BoundDeclaredClassTypeNode(custom, symbolRef);
                    } if (symbolRef.get() instanceof TypeAliasSymbol) {
                        yield new BoundAliasedTypeNode(custom, symbolRef);
                    } else {
                        addDiagnostic(BinderErrors.IdentifierIsNotType, type, custom.value);
                        yield new BoundInvalidTypeNode(custom);
                    }
                }

                Optional<Class<?>> optional = parameters.getCustomTypes().stream().filter(c -> {
                    return c.getAnnotation(CustomType.class).name().equals(custom.value);
                }).findFirst();
                if (optional.isPresent()) {
                    yield new BoundCustomTypeNode(custom, SType.fromJavaType(optional.get()));
                } else {
                    addDiagnostic(BinderErrors.TypeNotDefined, type, custom.value);
                    yield new BoundInvalidTypeNode(custom);
                }
            }

            case ARRAY_TYPE -> {
                ArrayTypeNode array = (ArrayTypeNode) type;
                BoundTypeNode underlying = bindType(array.underlying);
                yield new BoundArrayTypeNode(array, underlying);
            }

            case REF_TYPE -> {
                RefTypeNode ref = (RefTypeNode) type;
                BoundTypeNode underlying = bindType(ref.underlying);
                yield new BoundRefTypeNode(ref, underlying, underlying.type.getReferenceType());
            }

            case JAVA_TYPE -> {
                JavaTypeNode java = (JavaTypeNode) type;
                if (context.isJavaTypeUsageAllowed()) {
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(java.name.value, false, context.getJavaTypeClassLoader());
                    } catch (ClassNotFoundException e) {
                        clazz = null;
                    }
                    if (clazz != null) {
                        yield new BoundJavaTypeNode(java, SClassType.create(clazz));
                    } else {
                        addDiagnostic(BinderErrors.JavaTypeDoesNotExist, java, java.name.value);
                        yield new BoundJavaTypeNode(java, SUnknown.instance);
                    }
                } else {
                    addDiagnostic(BinderErrors.JavaTypeNotAllowed, java, context.getJavaTypeUsageError());
                    yield new BoundJavaTypeNode(java, SUnknown.instance);
                }
            }

            case FUNCTION_TYPE -> {
                FunctionTypeNode functionTypeNode = (FunctionTypeNode) type;
                BoundTypeNode returnTypeNode = bindType(functionTypeNode.returnTypeNode);
                List<BoundTypeNode> parameterTypeNodes = functionTypeNode.parameterTypes.getNodes().stream().map(this::bindType).toList();
                SGenericFunction functionType = context.getGenericFunction(returnTypeNode.type, parameterTypeNodes.stream().map(node -> node.type).toArray(SType[]::new));
                yield new BoundFunctionTypeNode(
                        functionTypeNode,
                        parameterTypeNodes,
                        returnTypeNode,
                        functionType);
            }

            case LET_TYPE -> {
                addDiagnostic(BinderErrors.LetInvalidContext, type);
                yield new BoundInvalidTypeNode((LetTypeNode) type);
            }

            default -> throw new InternalException();
        };
    }

    private BoundReturnStatementNode rewriteAsReturnStatement(ExpressionStatementNode expressionStatement, SType returnType) {
        BoundExpressionNode expression = bindExpressionOrThrow(expressionStatement.expression);
        BoundExpressionNode converted = convert(expression, returnType);
        Token semicolon = expressionStatement.semicolon;
        if (semicolon == null) {
            semicolon = new Token(TokenType.SEMICOLON, expression.getRange().getEnd());
        }
        ReturnStatementNode syntaxNode = new ReturnStatementNode(
                new Token(TokenType.RETURN, expression.getRange().getStart()),
                new InvalidExpressionNode(expression.getRange().getStart()),
                semicolon);
        return new BoundReturnStatementNode(syntaxNode, converted, expressionStatement.getRange());
    }

    private void buildDeclarationTable(CompilationUnitNode unit) {
        // process classes
        for (CompilationUnitMemberNode member : unit.members.members) {
            if (member.is(ParserNodeType.CLASS_DECLARATION)) {
                buildClassDeclaration((ClassNode) member);
            }
        }

        // process type aliases
        for (CompilationUnitMemberNode member : unit.members.members) {
            if (member.is(ParserNodeType.TYPE_ALIAS)) {
                buildTypeAliasDeclaration((TypeAliasNode) member);
            }
        }

        // resolve type aliases
        resolveTypeAliases();

        // process inheritance
        declarationTable.forEachClassDeclaration((classNode, classDeclaration) -> {
            if (classNode.baseTypeNode != null) {
                classDeclaration.setBaseType(bindType(classNode.baseTypeNode));

                SType baseType = classDeclaration.getDeclaredType().getBaseType();
                if (baseType.isInstanceOf(classDeclaration.getDeclaredType())) {
                    classDeclaration.getDeclaredType().clearBaseType();
                    addDiagnostic(BinderErrors.ClassCircularInheritance, classNode.baseTypeNode);
                }
            }
        });

        // process extensions
        for (CompilationUnitMemberNode member : unit.members.members) {
            if (member.is(ParserNodeType.EXTENSION_DECLARATION)) {
                buildExtensionDeclaration((ExtensionNode) member);
            }
        }

        // process static variables and functions
        for (CompilationUnitMemberNode member : unit.members.members) {
            switch (member.getNodeType()) {
                case CLASS_DECLARATION, EXTENSION_DECLARATION, TYPE_ALIAS -> {}
                case STATIC_VARIABLE -> buildStaticFieldDeclaration((StaticVariableNode) member);
                case FUNCTION -> buildFunctionDeclaration((FunctionNode) member);
                default -> throw new InternalException();
            }
        }

        // process class members
        declarationTable.forEachClassDeclaration((classNode, classDeclaration) -> {
            for (ClassMemberNode classMember : classNode.members) {
                switch (classMember.getNodeType()) {
                    case CLASS_FIELD -> buildClassFieldDeclaration(classDeclaration, (ClassFieldNode) classMember);
                    case CLASS_CONSTRUCTOR -> buildClassConstructorDeclaration(classDeclaration, (ClassConstructorNode) classMember);
                    case CLASS_METHOD -> buildClassMethodDeclaration(classDeclaration, (ClassMethodNode) classMember);
                    case CLASS_OPERATOR_OVERLOAD -> buildClassOperatorOverloadDeclaration(classDeclaration, (ClassOperatorOverloadNode) classMember);
                    default -> throw new InternalException();
                }
            }
        });

        // process extension methods
        declarationTable.forEachExtension((extensionNode, extensionDeclaration) -> {
            for (ClassMemberNode memberNode : extensionNode.members) {
                switch (memberNode.getNodeType()) {
                    case CLASS_METHOD -> buildExtensionMethodDeclaration(extensionDeclaration, (ClassMethodNode) memberNode);
                    case CLASS_OPERATOR_OVERLOAD -> buildExtensionOperatorOverloadDeclaration(extensionDeclaration, (ClassOperatorOverloadNode) memberNode);
                    default -> throw new InternalException();
                }
            }
        });
    }

    private void buildClassDeclaration(ClassNode classNode) {
        String name = classNode.name.value;
        if (!name.isEmpty() && declarationTable.hasSymbol(name)) {
            addDiagnostic(BinderErrors.SymbolAlreadyDeclared, classNode.name, name);
        }

        SDeclaredType declaredType = new SDeclaredType(name);
        ClassSymbol classSymbol = new ClassSymbol(name, declaredType, TextRange.combine(classNode.keyword, classNode.name));
        declarationTable.addClass(name, classNode, new ClassDeclaration(name, new ImmutableSymbolRef(classSymbol)));
    }

    private void buildTypeAliasDeclaration(TypeAliasNode typeAliasNode) {
        String name = typeAliasNode.name.value;
        if (!name.isEmpty() && declarationTable.hasSymbol(name)) {
            addDiagnostic(BinderErrors.SymbolAlreadyDeclared, typeAliasNode.name, name);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new TypeAliasSymbol(name, TextRange.combine(typeAliasNode.keyword, typeAliasNode.name)));
        declarationTable.addTypeAlias(name, typeAliasNode, new TypeAliasDeclaration(name, symbolRef));
    }

    private void resolveTypeAliases() {
        List<TypeAliasNode> unresolved = new ArrayList<>();
        for (CompilationUnitMemberNode member : unit.members.members) {
            if (member.is(ParserNodeType.TYPE_ALIAS)) {
                unresolved.add((TypeAliasNode) member);
            }
        }

        while (!unresolved.isEmpty()) {
            Iterator<TypeAliasNode> iterator = unresolved.iterator();
            while (iterator.hasNext()) {
                TypeAliasNode current = iterator.next();
                TypeAliasDeclaration declaration = declarationTable.getTypeAliasDeclaration(current);
                TypeAliasSymbol symbol = declaration.getSymbol();
                SAliasType aliasType = symbol.getAliasType();

                // get symbol ref if it is pointing to another alias
                TypeAliasSymbol linkedAliasSymbolRef = null;
                if (current.typeNode.is(ParserNodeType.CUSTOM_TYPE)) {
                    CustomTypeNode customTypeNode = (CustomTypeNode) current.typeNode;
                    SymbolRef symbolRef = getSymbol(customTypeNode.value);
                    if (symbolRef instanceof ImmutableSymbolRef immutableSymbolRef && immutableSymbolRef.get() instanceof TypeAliasSymbol linked) {
                        linkedAliasSymbolRef = linked;
                    }
                }

                if (linkedAliasSymbolRef != null) {
                    SAliasType linkedAliasType = linkedAliasSymbolRef.getAliasType();
                    if (aliasType.isFormingLoop(linkedAliasType)) {
                        addDiagnostic(BinderErrors.TypeAliasLoop, current);
                        iterator.remove();

                        BoundSymbolNode symbolNode = new BoundSymbolNode(current.name, declaration.getSymbolRef());
                        BoundTypeNode boundTypeNode = new BoundInvalidTypeNode((CustomTypeNode) current.typeNode);
                        BoundTypeAliasNode boundTypeAliasNode = new BoundTypeAliasNode(current, symbolNode, boundTypeNode);
                        declaration.setBound(boundTypeAliasNode);

                        aliasType.setUnderlying(SUnknown.instance);

                        continue;
                    }

                    aliasType.setUnderlying(linkedAliasType);

                    if (aliasType.canBeResolved()) {
                        BoundSymbolNode symbolNode = new BoundSymbolNode(current.name, declaration.getSymbolRef());
                        BoundTypeNode boundTypeNode = bindType(current.typeNode);
                        BoundTypeAliasNode boundTypeAliasNode = new BoundTypeAliasNode(current, symbolNode, boundTypeNode);
                        declaration.setBound(boundTypeAliasNode);

                        iterator.remove();
                    }
                } else {
                    BoundSymbolNode symbolNode = new BoundSymbolNode(current.name, declaration.getSymbolRef());
                    BoundTypeNode boundTypeNode = bindType(current.typeNode);
                    BoundTypeAliasNode boundTypeAliasNode = new BoundTypeAliasNode(current, symbolNode, boundTypeNode);
                    declaration.setBound(boundTypeAliasNode);
                    aliasType.setUnderlying(boundTypeNode.type);

                    iterator.remove();
                }
            }
        }
    }

    private void buildExtensionDeclaration(ExtensionNode extensionNode) {
        BoundTypeNode boundTypeNode = bindType(extensionNode.typeNode);
        declarationTable.addExtension(extensionNode, new ExtensionDeclaration(boundTypeNode));
    }

    private void buildStaticFieldDeclaration(StaticVariableNode fieldNode) {
        String name = fieldNode.name.value;
        boolean hasError = false;
        if (!name.isEmpty() && (declarationTable.hasSymbol(name) || context.hasSymbol(name))) {
            hasError = true;
            addDiagnostic(BinderErrors.SymbolAlreadyDeclared, fieldNode.name, name);
        }

        BoundTypeNode boundTypeNode = bindType(fieldNode.type);
        declarationTable.addStaticVariable(
                fieldNode,
                new StaticVariableDeclaration(name, boundTypeNode, hasError));
    }

    private void buildFunctionDeclaration(FunctionNode functionNode) {
        String name = functionNode.name.value;

        FunctionGroupDeclaration groupDeclaration = null;

        boolean hasGroupError = false;
        if (!name.isEmpty()) {
            if (context.hasSymbol(name)) {
                hasGroupError = true;
            } else if (declarationTable.hasSymbol(name)) {
                groupDeclaration = declarationTable.getFunctionGroupDeclaration(name);
                if (groupDeclaration == null) {
                    hasGroupError = true;
                }
            }
        }

        if (hasGroupError) {
            addDiagnostic(BinderErrors.SymbolAlreadyDeclared, functionNode.name, name);
        }

        TextRange definition = TextRange.combine(functionNode.modifiers, functionNode.parameters);
        if (groupDeclaration == null) {
            groupDeclaration = new FunctionGroupDeclaration(name, new ImmutableSymbolRef(new FunctionGroup(name, definition)), hasGroupError);
            declarationTable.addFunctionGroup(groupDeclaration);
        }

        boolean isAsync = functionNode.modifiers.isAsync();
        BoundTypeNode returnTypeNode = bindType(functionNode.returnType);
        SType actualReturnType = isAsync ? new SFuture(returnTypeNode.type) : returnTypeNode.type;
        BoundParameterListNode parameters = bindParameterList(functionNode.parameters);
        SStaticFunction functionType = new SStaticFunction(
                actualReturnType,
                parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));
        SymbolRef symbolRef = new ImmutableSymbolRef(new Function(name, functionType, TextRange.combine(functionNode.modifiers, functionNode.parameters)));

        boolean hasFunctionError = false;
        for (Function overload : groupDeclaration.getFunctionGroup().getFunctions()) {
            if (overload.getFunctionType().signatureMatchesExactly(functionType)) {
                hasFunctionError = true;
                addDiagnostic(BinderErrors.FunctionAlreadyDeclared, functionNode.name);
                break;
            }
        }

        declarationTable.addFunction(functionNode, new FunctionDeclaration(
                name,
                symbolRef,
                groupDeclaration,
                isAsync,
                returnTypeNode,
                parameters,
                hasFunctionError));
    }

    private void buildClassFieldDeclaration(ClassDeclaration classDeclaration, ClassFieldNode classFieldNode) {
        String fieldName = classFieldNode.name.value;
        BoundTypeNode typeNode = bindType(classFieldNode.type);

        boolean hasError = false;
        PropertyReference propertyRef = UnknownPropertyReference.instance;
        if (classDeclaration.hasMember(fieldName)) {
            hasError = true;
            addDiagnostic(BinderErrors.MemberAlreadyDeclared, classFieldNode.name, fieldName);
        } else {
            SType baseType = classDeclaration.getDeclaredType().getBaseType();
            if (baseType.getInstanceProperties().stream().anyMatch(p -> p.getName().equals(fieldName))) {
                addDiagnostic(BinderErrors.BaseClassAlreadyHasMember, classFieldNode.name);
            }
            if (baseType.getInstanceMethods().stream().anyMatch(m -> m.getName().equals(fieldName))) {
                addDiagnostic(BinderErrors.BaseClassAlreadyHasMember, classFieldNode.name);
            }

            propertyRef = classDeclaration.getDeclaredType().addField(typeNode.type, fieldName);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new ClassPropertySymbol(fieldName, typeNode.type, TextRange.combine(classFieldNode.type, classFieldNode.name)));
        classDeclaration.addField(classFieldNode, new ClassFieldDeclaration(fieldName, symbolRef, typeNode, propertyRef, hasError));
    }

    private void buildClassConstructorDeclaration(ClassDeclaration classDeclaration, ClassConstructorNode constructorNode) {
        BoundParameterListNode parameters = bindParameterList(constructorNode.parameters);
        SMethodFunction functionType = new SMethodFunction(SVoidType.instance, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        ConstructorReference constructorRef = UnknownConstructorReference.instance;
        if (classDeclaration.hasConstructor(parameters.parameters)) {
            hasError = true;
            addDiagnostic(BinderErrors.ConstructorAlreadyDeclared, constructorNode.keyword);
        } else {
            constructorRef = classDeclaration.getDeclaredType().addConstructor(functionType);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new ConstructorSymbol(functionType, constructorNode.keyword.getRange()));
        classDeclaration.addConstructor(constructorNode, new ClassConstructorDeclaration(symbolRef, parameters, constructorRef, hasError));
    }

    private void buildClassMethodDeclaration(ClassDeclaration classDeclaration, ClassMethodNode methodNode) {
        boolean isAsync = methodNode.modifiers.isAsync();
        BoundTypeNode typeNode = bindType(methodNode.type);
        SType actualReturnType = isAsync ? new SFuture(typeNode.type) : typeNode.type;
        String methodName = methodNode.name.value;
        BoundParameterListNode parameters = bindParameterList(methodNode.parameters);
        SMethodFunction functionType = new SMethodFunction(actualReturnType, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        MethodReference methodRef = UnknownMethodReference.instance;
        if (classDeclaration.hasField(methodName)) {
            hasError = true;
            addDiagnostic(BinderErrors.MemberAlreadyDeclared, methodNode.name, methodName);
        } else if (classDeclaration.hasMethod(methodName, parameters.parameters)) {
            hasError = true;
            addDiagnostic(BinderErrors.MethodAlreadyDeclared, methodNode.name);
        } else {
            SType baseType = classDeclaration.getDeclaredType().getBaseType();
            if (baseType.getInstanceProperties().stream().anyMatch(p -> p.getName().equals(methodName))) {
                addDiagnostic(BinderErrors.BaseClassAlreadyHasMember, methodNode.name);
            }
            MethodReference overrideCandidateBaseMethod = baseType.getInstanceMethods().stream()
                    .filter(m -> m.getName().equals(methodName) && m.signatureMatchesExactly(functionType))
                    .findFirst()
                    .orElse(null);
            if (overrideCandidateBaseMethod != null) {
                if (!overrideCandidateBaseMethod.getReturn().equals(actualReturnType)) {
                    addDiagnostic(BinderErrors.MethodOverrideReturnMismatch, methodNode.name);
                } else {
                    if (!methodNode.modifiers.isOverride()) {
                        addDiagnostic(BinderErrors.OverrideMissing, methodNode.name);
                    } else if (!overrideCandidateBaseMethod.isVirtual()) {
                        addDiagnostic(BinderErrors.NonVirtualOverride, methodNode.name);
                    } else if (overrideCandidateBaseMethod.isFinal()) {
                        addDiagnostic(BinderErrors.CannotOverrideFinal, methodNode.name);
                    }
                }
            }

            methodRef = classDeclaration.getDeclaredType().addMethod(methodNode.modifiers.toMemberModifiers(), functionType, methodName);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new MethodSymbol(methodName, functionType, TextRange.combine(methodNode.modifiers, methodNode.parameters)));
        classDeclaration.addMethod(methodNode, new ClassMethodDeclaration(methodName, symbolRef, isAsync, typeNode, parameters, methodRef, hasError));
    }

    private void buildClassOperatorOverloadDeclaration(ClassDeclaration classDeclaration, ClassOperatorOverloadNode overloadNode) {
        UnaryOperator unary = UnaryOperator.fromToken(overloadNode.operator.getTokenType());
        BinaryOperator binary = BinaryOperator.fromToken(overloadNode.operator.getTokenType());

        if (unary == null && binary == null) {
            throw new InternalException();
        }

        boolean processAsUnary;
        if (unary != null) {
            if (binary == null) {
                processAsUnary = true;
            } else {
                processAsUnary = overloadNode.parameters.parameters.size() == 1;
            }
        } else {
            processAsUnary = false;
        }

        if (processAsUnary) {
            buildClassUnaryOperatorOverloadDeclaration(classDeclaration, overloadNode, unary);
        } else {
            buildClassBinaryOperatorOverloadDeclaration(classDeclaration, overloadNode, binary);
        }
    }

    private void buildClassUnaryOperatorOverloadDeclaration(ClassDeclaration classDeclaration, ClassOperatorOverloadNode overloadNode, UnaryOperator operator) {
        BoundTypeNode returnTypeNode = bindType(overloadNode.returnType);
        BoundParameterListNode parameters = bindParameterList(overloadNode.parameters);
        SMethodFunction functionType = new SMethodFunction(returnTypeNode.type, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        MethodReference methodRef = UnknownMethodReference.instance;
        if (parameters.parameters.size() != 1) {
            hasError = true;
            addDiagnostic(BinderErrors.UnaryOperationOverloadOneParameters, parameters);
        } else if (!parameters.parameters.getFirst().getType().equals(classDeclaration.getDeclaredType())) {
            hasError = true;
            addDiagnostic(BinderErrors.UnaryOperationOverloadShouldHaveSameParameter, parameters.parameters.getFirst());
        } else if (classDeclaration.hasUnaryOperation(operator)) {
            hasError = true;
            addDiagnostic(BinderErrors.UnaryOperationAlreadyDeclared, parameters);
        } else {
            methodRef = classDeclaration.getDeclaredType().addUnaryOperation(operator, functionType);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new UnaryOperationSymbol(operator, functionType, TextRange.combine(overloadNode.returnType, overloadNode.parameters)));
        classDeclaration.addUnaryOperation(overloadNode, new ClassUnaryOperationDeclaration(returnTypeNode, operator, parameters, symbolRef, methodRef, hasError));
    }

    private void buildClassBinaryOperatorOverloadDeclaration(ClassDeclaration classDeclaration, ClassOperatorOverloadNode overloadNode, BinaryOperator operator) {
        BoundTypeNode returnTypeNode = bindType(overloadNode.returnType);
        BoundParameterListNode parameters = bindParameterList(overloadNode.parameters);
        SMethodFunction functionType = new SMethodFunction(returnTypeNode.type, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        MethodReference methodRef = UnknownMethodReference.instance;
        if (parameters.parameters.size() != 2) {
            hasError = true;
            addDiagnostic(BinderErrors.BinaryOperationOverloadTwoParameters, parameters);
        } else if (!parameters.parameters.getFirst().getType().equals(classDeclaration.getDeclaredType()) && !parameters.parameters.getLast().getType().equals(classDeclaration.getDeclaredType())) {
            hasError = true;
            addDiagnostic(BinderErrors.BinaryOperationOverloadShouldHaveOneParameterType, parameters);
        } else if (classDeclaration.hasBinaryOperation(operator, parameters.parameters.getFirst().getType(), parameters.parameters.getLast().getType())) {
            hasError = true;
            addDiagnostic(BinderErrors.BinaryOperationAlreadyDeclared, parameters);
        } else {
            methodRef = classDeclaration.getDeclaredType().addBinaryOperation(operator, functionType);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new BinaryOperationSymbol(operator, functionType, TextRange.combine(overloadNode.returnType, overloadNode.parameters)));
        classDeclaration.addBinaryOperation(overloadNode, new ClassBinaryOperationDeclaration(returnTypeNode, operator, parameters, symbolRef, methodRef, hasError));
    }

    private void buildExtensionMethodDeclaration(ExtensionDeclaration extensionDeclaration, ClassMethodNode methodNode) {
        boolean isAsync = methodNode.modifiers.isAsync();
        BoundTypeNode typeNode = bindType(methodNode.type);
        SType actualReturnType = isAsync ? new SFuture(typeNode.type) : typeNode.type;
        String methodName = methodNode.name.value;
        BoundParameterListNode parameters = bindParameterList(methodNode.parameters);
        SMethodFunction functionType = new SMethodFunction(actualReturnType, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        SType baseType = extensionDeclaration.getBaseType();

        boolean hasError = false;
        MethodReference methodRef = UnknownMethodReference.instance;
        if (baseType.getInstanceProperties().stream().anyMatch(p -> p.getName().equals(methodName))) {
            hasError = true;
            addDiagnostic(BinderErrors.MemberAlreadyDeclared, methodNode.name, methodName);
        } else if (hasInstanceMethod(baseType, methodName, parameters.parameters)) {
            hasError = true;
            addDiagnostic(BinderErrors.MethodAlreadyDeclared, methodNode.name);
        } else if (declarationTable.hasExtensionMethod(baseType, methodName, parameters.parameters)) {
            hasError = true;
            addDiagnostic(BinderErrors.ExtensionMethodAlreadyDeclared, methodNode.name);
        } else {
            String internalMethodName = declarationTable.generateExtensionMethodInternalName(baseType, methodName);
            ExtensionMethodReference extMethodReference = new ExtensionMethodReference(baseType, methodName, functionType, internalMethodName);
            methodRef = extMethodReference;
            declarationTable.addExtensionMethod(extMethodReference);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new MethodSymbol(methodName, functionType, TextRange.combine(methodNode.modifiers, methodNode.parameters)));
        extensionDeclaration.addMethod(methodNode, new ClassMethodDeclaration(methodName, symbolRef, isAsync, typeNode, parameters, methodRef, hasError));
    }

    private void buildExtensionOperatorOverloadDeclaration(ExtensionDeclaration extensionDeclaration, ClassOperatorOverloadNode overloadNode) {
        UnaryOperator unary = UnaryOperator.fromToken(overloadNode.operator.getTokenType());
        BinaryOperator binary = BinaryOperator.fromToken(overloadNode.operator.getTokenType());

        if (unary == null && binary == null) {
            throw new InternalException();
        }

        boolean processAsUnary;
        if (unary != null) {
            if (binary == null) {
                processAsUnary = true;
            } else {
                processAsUnary = overloadNode.parameters.parameters.size() == 1;
            }
        } else {
            processAsUnary = false;
        }

        if (processAsUnary) {
            buildExtensionUnaryOperatorOverloadDeclaration(extensionDeclaration, overloadNode, unary);
        } else {
            buildExtensionBinaryOperatorOverloadDeclaration(extensionDeclaration, overloadNode, binary);
        }
    }

    private void buildExtensionUnaryOperatorOverloadDeclaration(ExtensionDeclaration extensionDeclaration, ClassOperatorOverloadNode overloadNode, UnaryOperator operator) {
        BoundTypeNode returnTypeNode = bindType(overloadNode.returnType);
        BoundParameterListNode parameters = bindParameterList(overloadNode.parameters);
        SMethodFunction functionType = new SMethodFunction(returnTypeNode.type, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        UnaryOperation operation = UndefinedUnaryOperation.instance;
        if (parameters.parameters.size() != 1) {
            hasError = true;
            addDiagnostic(BinderErrors.UnaryOperationOverloadOneParameters, parameters);
        } else if (!parameters.parameters.getFirst().getType().equals(extensionDeclaration.getBaseType())) {
            hasError = true;
            addDiagnostic(BinderErrors.UnaryOperationOverloadShouldHaveSameParameter, parameters.parameters.getFirst());
        } else if (declarationTable.hasExtensionUnaryOperationOverload(extensionDeclaration.getBaseType(), operator)) {
            hasError = true;
            addDiagnostic(BinderErrors.UnaryOperationAlreadyDeclared, parameters);
        } else {
            String internalMethodName = declarationTable.generateExtensionOperationOverloadInternalName(extensionDeclaration.getBaseType(), operator.name().toLowerCase());
            ExtensionUnaryOperation extOperation = new ExtensionUnaryOperation(operator, returnTypeNode.type, extensionDeclaration.getBaseType(), internalMethodName);
            operation = extOperation;
            declarationTable.addExtensionUnaryOperation(extOperation);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new UnaryOperationSymbol(operator, functionType, TextRange.combine(overloadNode.returnType, overloadNode.parameters)));
        extensionDeclaration.addUnaryOperation(overloadNode, new ExtensionUnaryOperationDeclaration(returnTypeNode, parameters, symbolRef, operation, hasError));
    }

    private void buildExtensionBinaryOperatorOverloadDeclaration(ExtensionDeclaration extensionDeclaration, ClassOperatorOverloadNode overloadNode, BinaryOperator operator) {
        BoundTypeNode returnTypeNode = bindType(overloadNode.returnType);
        BoundParameterListNode parameters = bindParameterList(overloadNode.parameters);
        SMethodFunction functionType = new SMethodFunction(returnTypeNode.type, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        BinaryOperation operation = UndefinedBinaryOperation.instance;
        if (parameters.parameters.size() != 2) {
            hasError = true;
            addDiagnostic(BinderErrors.BinaryOperationOverloadTwoParameters, parameters);
        } else if (!parameters.parameters.getFirst().getType().equals(extensionDeclaration.getBaseType()) && !parameters.parameters.getLast().getType().equals(extensionDeclaration.getBaseType())) {
            hasError = true;
            addDiagnostic(BinderErrors.BinaryOperationOverloadShouldHaveOneParameterType, parameters);
        } else if (declarationTable.hasExtensionBinaryOperationOverload(operator, parameters.parameters.getFirst().getType(), parameters.parameters.getLast().getType())) {
            hasError = true;
            addDiagnostic(BinderErrors.BinaryOperationAlreadyDeclared, parameters);
        } else {
            String internalMethodName = declarationTable.generateExtensionOperationOverloadInternalName(extensionDeclaration.getBaseType(), operator.name().toLowerCase());
            ExtensionBinaryOperation extOperation = new ExtensionBinaryOperation(operator, returnTypeNode.type, parameters.parameters.getFirst().getType(), parameters.parameters.getLast().getType(), internalMethodName);
            operation = extOperation;
            declarationTable.addExtensionBinaryOperation(extOperation);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new BinaryOperationSymbol(operator, functionType, TextRange.combine(overloadNode.returnType, overloadNode.parameters)));
        extensionDeclaration.addBinaryOperation(overloadNode, new ExtensionBinaryOperationDeclaration(returnTypeNode, parameters, symbolRef, operation, hasError));
    }

    private boolean hasInstanceMethod(SType type, String name, List<BoundParameterNode> parameters) {
        for (MethodReference method : type.getInstanceMethods()) {
            if (!method.getName().equals(name)) {
                continue;
            }

            List<SType> parameterTypes = method.getParameterTypes();
            if (parameterTypes.size() != parameters.size()) {
                continue;
            }

            boolean same = true;
            for (int i = 0; i < parameters.size(); i++) {
                if (!parameterTypes.get(i).equals(parameters.get(i).getType())) {
                    same = false;
                    break;
                }
            }

            if (same) {
                return true;
            }
        }

        return false;
    }

    private void addVariablesToContext(List<SymbolRef> refs) {
        for (SymbolRef ref : refs) {
            context.addLocalVariable(ref);
        }
    }

    @Nullable
    private SymbolRef getSymbol(String name) {
        SymbolRef symbolRef = context.getSymbol(name);
        if (symbolRef != null) {
            return symbolRef;
        }

        return declarationTable.getSymbol(name);
    }

    private void pushScope() {
        context = context.createChild();
    }

    private void pushScope(Frame frame) {
        context = context.createChild(frame);
    }

    private void pushFunctionScope(SType returnType, boolean isAsync) {
        context = context.createInstanceMethod(returnType, isAsync);
    }

    private void pushStaticFunctionScope(SType returnType, boolean isAsync) {
        context = context.createStaticFunction(returnType, isAsync);
    }

    private void pushClassScope(SDeclaredType type) {
        context = context.createClass(type);
    }

    private void pushExtensionScope(SType type) {
        context = context.createExtension(type);
    }

    private void pushConstructorScope() {
        context = context.createClassMethod(SVoidType.instance, false);
    }

    private void pushMethodScope(SType returnType, boolean isAsync) {
        context = context.createClassMethod(returnType, isAsync);
    }

    private void pushStaticMethodScope(SType returnType) {
        context = context.createClassStaticMethod(returnType);
    }

    private void popScope() {
        context = context.getParent();
    }

    private void addParametersToContext(BoundParameterListNode parameterListNode) {
        for (BoundParameterNode parameter : parameterListNode.parameters) {
            BoundTypeNode typeNode = parameter.getTypeNode();
            parameter.getName().symbolRef.set(typeNode instanceof BoundRefTypeNode ref ?
                    context.addLocalRefParameter(parameter.getName().value, (SByReference) ref.type, ref.underlying.type, parameter.getRange()) :
                    context.addLocalParameter(parameter.getName().value, typeNode.type, parameter.getRange()));
        }
    }

    private void addDiagnostic(ErrorCode code, Locatable locatable, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, locatable, parameters));
    }

    private int parseHex(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return 10 + ch - 'A';
        }
        if ('a' <= ch && ch <= 'f') {
            return 10 + ch - 'a';
        }
        throw new InternalException();
    }

    private record ExpressionPair(boolean result, BoundExpressionNode expression1, BoundExpressionNode expression2) {
        public ExpressionPair(BoundExpressionNode expression1, BoundExpressionNode expression2) {
            this(true, expression1, expression2);
        }
    }

    private record ArgumentsCast<T extends Invocable>(T invocable, List<ConversionInfo> conversions, int count) {}

    private record BindInvocableArgsResult<T extends Invocable>(
            T invocable,
            BoundArgumentsListNode argumentsListNode,
            boolean noInvocables,
            boolean noOverload) {}

    private record PatternFlow(
            BoundPatternNode pattern,
            List<SymbolRef> whenTrueLocals,
            List<SymbolRef> whenFalseLocals
    ) {
        public PatternFlow(BoundPatternNode pattern) {
            this(pattern, List.of(), List.of());
        }
    }

    private record BinaryOperationResolveResult(
            @Nullable CastOperation leftCast,
            BinaryOperation operation,
            @Nullable CastOperation rightCast
    ) {
        public BinaryOperationResolveResult(BinaryOperation operation) {
            this(null, operation, null);
        }
    }
}