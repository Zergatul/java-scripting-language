package com.zergatul.scripting.binding;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.*;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.*;

import java.util.*;

public class Binder {

    private final String code;
    private final CompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;
    private final CompilationParameters parameters;
    private CompilerContext context;
    private DeclarationTable declarationTable;

    public Binder(ParserOutput input, CompilationParameters parameters) {
        this.code = input.code();
        this.unit = input.unit();
        this.diagnostics = input.diagnostics();
        this.parameters = parameters;
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
                new BoundCompilationUnitMembersListNode(members, node.members.getRange()),
                statements,
                node.getRange());
    }

    private List<BoundCompilationUnitMemberNode> bindCompilationUnitMembers(List<CompilationUnitMemberNode> nodes) {
        List<BoundCompilationUnitMemberNode> boundMembers = new ArrayList<>();
        for (CompilationUnitMemberNode member : nodes) {
            boundMembers.add(switch (member.getNodeType()) {
                case STATIC_VARIABLE -> bindStaticVariable((StaticVariableNode) member);
                case FUNCTION -> bindFunction((FunctionNode) member);
                case CLASS_DECLARATION -> bindClass((ClassNode) member);
                default -> throw new InternalException();
            });
        }
        return boundMembers;
    }

    private BoundStaticVariableNode bindStaticVariable(StaticVariableNode staticVariableNode) {
        StaticVariableDeclaration declaration = declarationTable.getStaticVariableDeclaration(staticVariableNode);
        if (declaration == null) {
            throw new InternalException();
        }

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

        BoundNameExpressionNode name = new BoundNameExpressionNode(
                symbolRef,
                typeNode.type,
                declaration.name(),
                staticVariableNode.name.getRange());

        return new BoundStaticVariableNode(
                typeNode,
                name,
                expression,
                staticVariableNode);
    }

    private BoundFunctionNode bindFunction(FunctionNode functionNode) {
        FunctionDeclaration declaration = declarationTable.getFunctionDeclaration(functionNode);
        if (declaration == null) {
            throw new InternalException();
        }

        pushStaticFunctionScope(declaration.getReturnType(), declaration.isAsync());
        addParametersToContext(declaration.getParameters());

        if (!declaration.hasError()) {
            context.getParent().addStaticSymbol(declaration.getName(), declaration.getSymbolRef());
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(declaration.getSymbolRef(), functionNode.name.getRange());

        BoundStatementNode statement;
        if (declaration.getReturnType() != SVoidType.instance && functionNode.body.getNodeType() == NodeType.EXPRESSION_STATEMENT) {
            // rewrite to return statement
            ExpressionStatementNode expressionStatement = (ExpressionStatementNode) functionNode.body;
            BoundExpressionNode expression = bindExpression(expressionStatement.expression);
            BoundExpressionNode converted = convert(expression, declaration.getReturnType());
            statement = new BoundReturnStatementNode(null, converted, functionNode.body.getRange());
        } else {
            statement = bindStatement(functionNode.body);
        }

        if (declaration.getReturnType() != SVoidType.instance && declaration.getReturnType() != SUnknown.instance) {
            if (!new ReturnPathsVerifier().verify(List.of(statement))) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, statement);
            }
        }

        BoundFunctionNode boundFunction = new BoundFunctionNode(
                declaration.isAsync(),
                declaration.getReturnTypeNode(),
                name,
                declaration.getParameters(),
                statement,
                context.getLifted(),
                functionNode);

        popScope();

        return boundFunction;
    }

    private BoundClassNode bindClass(ClassNode classNode) {
        ClassDeclaration declaration = declarationTable.getClassDeclaration(classNode);
        if (declaration == null) {
            throw new InternalException();
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(declaration.getSymbolRef(), classNode.name.getRange());
        List<BoundClassMemberNode> members = new ArrayList<>();
        pushClassScope(declaration.getDeclaredType());

        for (ClassMemberNode member : classNode.members) {
            members.add(switch (member.getNodeType()) {
                case CLASS_FIELD -> bindClassField(declaration, (ClassFieldNode) member);
                case CLASS_CONSTRUCTOR -> bindClassConstructor(declaration, (ClassConstructorNode) member);
                case CLASS_METHOD -> bindClassMethod(declaration, (ClassMethodNode) member);
                default -> throw new InternalException();
            });
        }

        popScope();
        return new BoundClassNode(name, members, classNode.getRange());
    }

    private BoundClassFieldNode bindClassField(ClassDeclaration classDeclaration, ClassFieldNode fieldNode) {
        ClassFieldDeclaration fieldDeclaration = classDeclaration.getFieldDeclaration(fieldNode);
        if (fieldDeclaration == null) {
            throw new InternalException();
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(fieldDeclaration.getSymbolRef(), fieldNode.name.getRange());
        return new BoundClassFieldNode(fieldDeclaration.getTypeNode(), name, fieldNode.getRange());
    }

    private BoundClassConstructorNode bindClassConstructor(ClassDeclaration classDeclaration, ClassConstructorNode constructorNode) {
        ClassConstructorDeclaration constructorDeclaration = classDeclaration.getConstructorDeclaration(constructorNode);
        if (constructorDeclaration == null) {
            throw new InternalException();
        }

        pushConstructorScope();
        addParametersToContext(constructorDeclaration.getParameters());
        BoundStatementNode body = bindStatement(constructorNode.body);
        popScope();

        return new BoundClassConstructorNode(
                (SMethodFunction) constructorDeclaration.getSymbolRef().get().getType(),
                constructorDeclaration.getParameters(),
                body,
                constructorNode.getRange());
    }

    private BoundClassMethodNode bindClassMethod(ClassDeclaration classDeclaration, ClassMethodNode methodNode) {
        ClassMethodDeclaration methodDeclaration = classDeclaration.getMethodDeclaration(methodNode);
        if (methodDeclaration == null) {
            throw new InternalException();
        }

        SType returnType = methodDeclaration.getTypeNode().type;

        pushMethodScope(returnType, methodDeclaration.isAsync());
        addParametersToContext(methodDeclaration.getParameters());

        BoundStatementNode body;
        if (returnType != SVoidType.instance && methodNode.body.getNodeType() == NodeType.EXPRESSION_STATEMENT) {
            // rewrite to return statement
            ExpressionStatementNode expressionStatement = (ExpressionStatementNode) methodNode.body;
            BoundExpressionNode expression = bindExpression(expressionStatement.expression);
            BoundExpressionNode converted = convert(expression, returnType);
            body = new BoundReturnStatementNode(null, converted, methodNode.body.getRange());
        } else {
            body = bindStatement(methodNode.body);
        }

        popScope();

        if (returnType != SVoidType.instance && returnType != SUnknown.instance) {
            if (!new ReturnPathsVerifier().verify(List.of(body))) {
                addDiagnostic(BinderErrors.NotAllPathReturnValue, body);
            }
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(methodDeclaration.getSymbolRef(), methodNode.name.getRange());
        return new BoundClassMethodNode(
                methodDeclaration.isAsync(),
                (SMethodFunction) methodDeclaration.getSymbolRef().get().getType(),
                methodDeclaration.getTypeNode(),
                name,
                methodDeclaration.getParameters(),
                body,
                methodNode.getRange());
    }

    private BoundParameterListNode bindParameterList(ParameterListNode node) {
        List<BoundParameterNode> parameters = new ArrayList<>(node.parameters.size());
        for (ParameterNode parameter : node.parameters) {
            BoundTypeNode type = bindType(parameter.getType());
            BoundNameExpressionNode name = new BoundNameExpressionNode(new ForwardSymbolRef(), type.type, parameter.getName().value, parameter.getName().getRange());

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
        return new BoundParameterListNode(parameters, node);
    }

    private BoundStatementsListNode bindStatementList(StatementsListNode node) {
        List<BoundStatementNode> statements = node.statements.stream().map(this::bindStatement).toList();
        return new BoundStatementsListNode(statements, context.getLifted(), node.getRange());
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
            default -> throw new InternalException();
        };
    }

    private BoundBlockStatementNode bindBlockStatement(BlockStatementNode block) {
        pushScope();
        List<BoundStatementNode> statements = block.statements.stream().map(this::bindStatement).toList();
        popScope();
        return new BoundBlockStatementNode(statements, block.getRange());
    }

    private BoundStatementNode bindAssignmentStatement(AssignmentStatementNode statement) {
        BoundExpressionNode left = bindExpression(statement.left);
        if (!left.canSet()) {
            addDiagnostic(BinderErrors.ExpressionCannotBeSet, statement.left);
        }

        BoundAssignmentOperatorNode operator = new BoundAssignmentOperatorNode(statement.operator.operator, statement.operator.getRange());
        BoundExpressionNode right = bindExpression(statement.right);

        if (operator.operator == AssignmentOperator.ASSIGNMENT) {
            right = convert(right, left.type);
            return new BoundAssignmentStatementNode(left, operator, right, statement);
        }

        BinaryOperation operation = left.type.binary(operator.operator.getBinaryOperator(), right.type);
        if (operation == null) {
            // try implicit cast right to left
            if (!left.type.equals(right.type)) {
                CastOperation cast = SType.implicitCastTo(right.type, left.type);
                if (cast != null) {
                    operation = left.type.binary(operator.operator.getBinaryOperator(), left.type);
                    if (operation != null) {
                        right = new BoundImplicitCastExpressionNode(right, cast, right.getRange());
                    }
                }
            }
        }

        if (operation != null) {
            if (!operation.type.equals(left.type)) {
                addDiagnostic(
                        BinderErrors.AugmentedAssignmentInvalidType,
                        operator,
                        operator.operator.getBinaryOperator(),
                        left.type,
                        right.type,
                        operation.type);
            }
        } else {
            operation = UndefinedBinaryOperation.instance;
            addDiagnostic(
                    BinderErrors.BinaryOperatorNotDefined,
                    operator,
                    operator.operator.getBinaryOperator(),
                    left.type.toString(),
                    right.type.toString());
        }

        return new BoundAugmentedAssignmentStatementNode(
                left,
                operator,
                new BoundBinaryOperatorNode(operation, operator.getRange()),
                right,
                statement.getRange());
    }

    private BoundVariableDeclarationNode bindVariableDeclaration(VariableDeclarationNode variableDeclaration) {
        BoundTypeNode variableType;
        BoundExpressionNode expression;

        if (variableDeclaration.type.getNodeType() == NodeType.LET_TYPE) {
            if (variableDeclaration.expression != null) {
                expression = bindExpression(variableDeclaration.expression);
                if (expression.getNodeType() == NodeType.UNCONVERTED_LAMBDA) {
                    addDiagnostic(BinderErrors.LetUnboundLambda, variableDeclaration.type);
                }
                if (expression.getNodeType() == NodeType.EMPTY_COLLECTION_EXPRESSION) {
                    addDiagnostic(BinderErrors.LetEmptyCollection, variableDeclaration.type);
                }
            } else {
                TextRange range = variableDeclaration.name.getRange();
                expression = new BoundInvalidExpressionNode(List.of(), new SingleLineTextRange(range.getLine1(), range.getColumn1(), range.getPosition(), 0));
            }
            variableType = new BoundLetTypeNode(expression.type, variableDeclaration.type.getRange());
        } else {
            variableType = bindType(variableDeclaration.type);
            if (variableDeclaration.expression != null) {
                expression = convert(bindExpression(variableDeclaration.expression), variableType.type);
            } else {
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
            LocalVariable variable = new LocalVariable(variableDeclaration.name.value, variableType.type, variableDeclaration.getRange());
            symbolRef = new MutableSymbolRef(variable);
            context.addLocalVariable(symbolRef);
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(
                symbolRef,
                variableType.type,
                variableDeclaration.name.value,
                variableDeclaration.name.getRange());

        return new BoundVariableDeclarationNode(variableType, name, expression, variableDeclaration);
    }

    private BoundExpressionStatementNode bindExpressionStatement(ExpressionStatementNode statement) {
        return new BoundExpressionStatementNode(bindExpression(statement.expression), statement.getRange());
    }

    private BoundIfStatementNode bindIfStatement(IfStatementNode statement) {
        BoundExpressionNode condition = convert(bindExpression(statement.condition), SBoolean.instance);
        BoundStatementNode thenStatement = bindStatement(statement.thenStatement);
        BoundStatementNode elseStatement = statement.elseStatement == null ? null : bindStatement(statement.elseStatement);
        return new BoundIfStatementNode(condition, thenStatement, elseStatement, statement);
    }

    private BoundReturnStatementNode bindReturnStatement(ReturnStatementNode statement) {
        if (statement.expression == null) {
            if (context.getReturnType() == SVoidType.instance) {
                return new BoundReturnStatementNode(statement.keyword, null, statement.getRange());
            } else {
                addDiagnostic(
                        BinderErrors.EmptyReturnStatement,
                        statement);
                return new BoundReturnStatementNode(statement.keyword, new BoundInvalidExpressionNode(List.of(), statement.getRange().subRange(6)), statement.getRange());
            }
        } else {
            BoundExpressionNode expression = bindExpression(statement.expression);
            SType expected = context.getReturnType();
            return new BoundReturnStatementNode(statement.keyword, convert(expression, expected), statement.getRange());
        }
    }

    private BoundForLoopStatementNode bindForLoopStatement(ForLoopStatementNode statement) {
        pushScope();
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

        context.setBreak(v -> {});
        context.setContinue(v -> {});
        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundForLoopStatementNode(statement.openParenthesis, statement.closeParenthesis, init, condition, update, body, statement.getRange());
    }

    private BoundForEachLoopStatementNode bindForEachLoopStatement(ForEachLoopStatementNode statement) {
        pushScope();

        SymbolRef index = context.addLocalVariable(null, SInt.instance, null);
        SymbolRef length = context.addLocalVariable(null, SInt.instance, null);

        BoundTypeNode variableType;

        BoundExpressionNode iterable = bindExpression(statement.iterable);
        if (iterable.type instanceof SArrayType arrayType) {
            if (statement.typeNode.getNodeType() == NodeType.LET_TYPE) {
                variableType = new BoundLetTypeNode(arrayType.getElementsType(), statement.typeNode.getRange());
            } else {
                variableType = bindType(statement.typeNode);
                if (!arrayType.getElementsType().equals(variableType.type)) {
                    addDiagnostic(BinderErrors.ForEachTypesNotMatch, statement.typeNode);
                }
            }
        } else {
            addDiagnostic(BinderErrors.CannotIterate, iterable, iterable.type.toString());

            if (statement.typeNode.getNodeType() == NodeType.LET_TYPE) {
                variableType = new BoundLetTypeNode(SUnknown.instance, statement.typeNode.getRange());
            } else {
                variableType = bindType(statement.typeNode);
            }
        }

        SymbolRef existingRef = context.getSymbol(statement.name.value);
        BoundNameExpressionNode name = null;
        if (existingRef != null) {
            addDiagnostic(
                    BinderErrors.SymbolAlreadyDeclared,
                    statement.name,
                    statement.name.value);
        } else {
            SymbolRef symbolRef = context.addLocalVariable(statement.name.value, variableType.type, TextRange.combine(statement.typeNode, statement.name));
            name = new BoundNameExpressionNode(symbolRef, statement.name.getRange());
        }

        context.setBreak(v -> {});
        context.setContinue(v -> {});
        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundForEachLoopStatementNode(
                variableType, name, iterable,
                body,
                index, length,
                statement);
    }

    private BoundWhileLoopStatementNode bindWhileLoopStatement(WhileLoopStatementNode statement) {
        pushScope();

        BoundExpressionNode condition = convert(bindExpression(statement.condition), SBoolean.instance);
        if (condition.type != SBoolean.instance) {
            addDiagnostic(BinderErrors.CannotImplicitlyConvert, condition, condition.type.toString(), SBoolean.instance.toString());
        }

        context.setBreak(v -> {});
        context.setContinue(v -> {});
        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundWhileLoopStatementNode(condition, body, statement);
    }

    private BoundBreakStatementNode bindBreakStatement(BreakStatementNode statement) {
        if (!context.canBreak()) {
            addDiagnostic(BinderErrors.NoLoop, statement);
        }
        return new BoundBreakStatementNode(statement.getRange());
    }

    private BoundContinueStatementNode bindContinueStatement(ContinueStatementNode statement) {
        if (!context.canContinue()) {
            addDiagnostic(BinderErrors.NoLoop, statement);
        }
        return new BoundContinueStatementNode(statement.getRange());
    }

    private BoundEmptyStatementNode bindEmptyStatement(EmptyStatementNode statement) {
        return new BoundEmptyStatementNode(statement.getRange());
    }

    private BoundInvalidStatementNode bindInvalidStatement(InvalidStatementNode statement) {
        return new BoundInvalidStatementNode(statement.getRange());
    }

    private BoundPostfixStatementNode bindPostfixStatement(PostfixStatementNode statement) {
        BoundExpressionNode expression = bindExpression(statement.expression);
        if (!expression.canSet()) {
            addDiagnostic(BinderErrors.ExpressionCannotBeSet, expression);
        }

        boolean isInc = statement.getNodeType() == NodeType.INCREMENT_STATEMENT;

        SType type = expression.type;
        PostfixOperation operation = isInc ? type.increment() : type.decrement();
        if (operation == null) {
            addDiagnostic(
                    BinderErrors.CannotApplyIncDec,
                    statement,
                    isInc ? "++" : "--",
                    type.toString());
        }

        return new BoundPostfixStatementNode(statement.getNodeType(), expression, operation, statement.getRange());
    }

    private BoundExpressionNode bindExpression(ExpressionNode expression) {
        return switch (expression.getNodeType()) {
            case BOOLEAN_LITERAL -> bindBooleanLiteralExpression((BooleanLiteralExpressionNode) expression);
            case INTEGER_LITERAL -> bindIntegerLiteralExpression((IntegerLiteralExpressionNode) expression);
            case INTEGER64_LITERAL -> bindInteger64LiteralExpression((Integer64LiteralExpressionNode) expression);
            case FLOAT_LITERAL -> bindFloatLiteralExpression((FloatLiteralExpressionNode) expression);
            case STRING_LITERAL -> bindStringLiteralExpression((StringLiteralExpressionNode) expression);
            case CHAR_LITERAL -> bindCharLiteralExpression((CharLiteralExpressionNode) expression);
            case PARENTHESIZED_EXPRESSION -> bindParenthesizedExpression((ParenthesizedExpressionNode) expression);
            case UNARY_EXPRESSION -> bindUnaryExpression((UnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> bindBinaryExpression((BinaryExpressionNode) expression);
            case TYPE_TEST_EXPRESSION -> bindTypeTestExpression((TypeTestExpressionNode) expression);
            case TYPE_CAST_EXPRESSION -> bindTypeCastExpression((TypeCastExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> bindConditionalExpression((ConditionalExpressionNode) expression);
            case INDEX_EXPRESSION -> bindIndexExpression((IndexExpressionNode) expression);
            case INVOCATION_EXPRESSION -> bindInvocationExpression((InvocationExpressionNode) expression);
            case NAME_EXPRESSION -> bindNameExpression((NameExpressionNode) expression);
            case THIS_EXPRESSION -> bindThisExpression((ThisExpressionNode) expression);
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
            case META_TYPE_EXPRESSION -> bindMetaTypeExpression((MetaTypeExpressionNode) expression);
            case META_TYPE_OF_EXPRESSION -> bindMetaTypeOfExpression((MetaTypeOfExpressionNode) expression);
            case INVALID_EXPRESSION -> bindInvalidExpression((InvalidExpressionNode) expression);
            default -> throw new InternalException();
        };
    }

    private BoundParenthesizedExpressionNode bindParenthesizedExpression(ParenthesizedExpressionNode parenthesizedExpression) {
        return new BoundParenthesizedExpressionNode(bindExpression(parenthesizedExpression.inner), parenthesizedExpression.getRange());
    }

    private BoundUnaryExpressionNode bindUnaryExpression(UnaryExpressionNode unary) {
        BoundExpressionNode operand = bindExpression(unary.operand);
        UnaryOperation operation = operand.type.unary(unary.operator.operator);
        BoundUnaryOperatorNode operator;
        if (operation != null) {
            operator = new BoundUnaryOperatorNode(operation, unary.operator.getRange());
        } else {
            addDiagnostic(
                    BinderErrors.UnaryOperatorNotDefined,
                    unary,
                    unary.operator.operator.toString(),
                    operand.type.toString());
            operator = new BoundUnaryOperatorNode(UndefinedUnaryOperation.instance, unary.operator.getRange());
        }
        return new BoundUnaryExpressionNode(operator, operand, unary.getRange());
    }

    private BoundBinaryExpressionNode bindBinaryExpression(BinaryExpressionNode binary) {
        BoundExpressionNode left = bindExpression(binary.left);
        BoundExpressionNode right = bindExpression(binary.right);
        BinaryOperation operation = left.type.binary(binary.operator.operator, right.type);
        if (operation != null) {
            BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(operation, binary.operator.getRange());
            return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
        } else {
            // try implicit cast arguments to each other and see if operator is defined
            if (!left.type.equals(right.type)) {
                CastOperation cast = SType.implicitCastTo(right.type, left.type);
                if (cast != null) {
                    operation = left.type.binary(binary.operator.operator, left.type);
                    if (operation != null) {
                        right = new BoundImplicitCastExpressionNode(right, cast, right.getRange());
                        BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(operation, binary.operator.getRange());
                        return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
                    }
                }

                cast = SType.implicitCastTo(left.type, right.type);
                if (cast != null) {
                    operation = right.type.binary(binary.operator.operator, right.type);
                    if (operation != null) {
                        left = new BoundImplicitCastExpressionNode(left, cast, left.getRange());
                        BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(operation, binary.operator.getRange());
                        return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
                    }
                }
            }

            addDiagnostic(
                    BinderErrors.BinaryOperatorNotDefined,
                    binary,
                    binary.operator.operator.toString(),
                    left.type.toString(),
                    right.type.toString());
            BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(UndefinedBinaryOperation.instance, binary.operator.getRange());
            return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
        }
    }

    private BoundTypeTestExpressionNode bindTypeTestExpression(TypeTestExpressionNode test) {
        BoundExpressionNode expression = bindExpression(test.expression);
        BoundTypeNode type = bindType(test.type);
        return new BoundTypeTestExpressionNode(expression, type, test.getRange());
    }

    private BoundTypeCastExpressionNode bindTypeCastExpression(TypeCastExpressionNode test) {
        BoundExpressionNode expression = bindExpression(test.expression);
        BoundTypeNode type = bindType(test.type);
        return new BoundTypeCastExpressionNode(expression, type, test.getRange());
    }

    private BoundBooleanLiteralExpressionNode bindBooleanLiteralExpression(BooleanLiteralExpressionNode bool) {
        return new BoundBooleanLiteralExpressionNode(bool.value, bool.getRange());
    }

    private BoundIntegerLiteralExpressionNode bindIntegerLiteralExpression(IntegerLiteralExpressionNode literal) {
        int value = 0;
        if (literal.value.startsWith("0x")) {
            if (literal.value.length() > 8 + 2) {
                addDiagnostic(BinderErrors.IntegerConstantTooLarge, literal);
                return new BoundIntegerLiteralExpressionNode(0, literal.getRange());
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

        return new BoundIntegerLiteralExpressionNode(value, literal.getRange());
    }

    private BoundInteger64LiteralExpressionNode bindInteger64LiteralExpression(Integer64LiteralExpressionNode literal) {
        String str = literal.value.substring(0, literal.value.length() - 1); // remove L

        long value = 0;
        if (str.startsWith("0x")) {
            if (str.length() > 16 + 2) {
                addDiagnostic(BinderErrors.IntegerConstantTooLarge, literal);
                return new BoundInteger64LiteralExpressionNode(0, literal.getRange());
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

        return new BoundInteger64LiteralExpressionNode(value, literal.getRange());
    }

    private BoundFloatLiteralExpressionNode bindFloatLiteralExpression(FloatLiteralExpressionNode literal) {
        double value;
        try {
            value = Double.parseDouble(literal.value);
        } catch (NumberFormatException e) {
            value = 0;
            addDiagnostic(BinderErrors.InvalidFloatConstant, literal);
        }
        return new BoundFloatLiteralExpressionNode(value, literal.getRange());
    }

    private BoundStringLiteralExpressionNode bindStringLiteralExpression(StringLiteralExpressionNode literal) {
        return new BoundStringLiteralExpressionNode(literal.value, literal.getRange());
    }

    private BoundCharLiteralExpressionNode bindCharLiteralExpression(CharLiteralExpressionNode literal) {
        return new BoundCharLiteralExpressionNode(literal.value, literal.getRange());
    }

    private BoundConditionalExpressionNode bindConditionalExpression(ConditionalExpressionNode expression) {
        BoundExpressionNode condition = convert(bindExpression(expression.condition), SBoolean.instance);
        BoundExpressionNode whenTrue = bindExpression(expression.whenTrue);
        BoundExpressionNode whenFalse = bindExpression(expression.whenFalse);

        ExpressionPair pair = tryCastToCommon(whenTrue, whenFalse);
        if (!pair.result) {
            addDiagnostic(
                    BinderErrors.CannotDetermineConditionalExpressionType,
                    expression,
                    whenTrue.type.toString(), whenFalse.type.toString());
        }

        return new BoundConditionalExpressionNode(condition, pair.expression1, pair.expression2, expression.getRange());
    }

    private BoundExpressionNode bindInvocationExpression(InvocationExpressionNode invocation) {
        BoundExpressionNode callee = bindExpression(invocation.callee);

        if (callee.getNodeType() == NodeType.METHOD_GROUP) {
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

            BoundMethodNode methodNode = new BoundMethodNode(result.invocable, methodGroup.method.getRange());
            return new BoundMethodInvocationExpressionNode(
                    methodGroup.callee,
                    methodGroup.dot,
                    methodNode,
                    result.argumentsListNode,
                    context.releaseRefVariables(),
                    invocation.getRange());
        }

        if (callee.type instanceof SStaticFunction staticFunction) {
            BoundFunctionReferenceNode functionReferenceNode = (BoundFunctionReferenceNode) callee;

            BindInvocableArgsResult<Function> result = bindInvocableArguments(
                    invocation.arguments,
                    List.of(functionReferenceNode.getFunction()));

            if (result.noOverload) {
                addDiagnostic(
                        BinderErrors.ArgumentCountMismatch,
                        functionReferenceNode,
                        functionReferenceNode.name,
                        staticFunction.getParameters().size());
            }

            return new BoundFunctionInvocationExpression(
                    functionReferenceNode,
                    staticFunction.getReturnType(),
                    result.argumentsListNode,
                    context.releaseRefVariables(),
                    invocation.getRange());
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
        return new BoundInvalidExpressionNode(List.of(callee), invocation.getRange());

        /*if (invocation.callee instanceof MemberAccessExpressionNode memberAccess) {
            // method invocation
            BoundExpressionNode objectReference = bindExpression(memberAccess.callee);
            // get methods by name
            List<MethodReference> methodReferences;
            if (objectReference.type == SUnknown.instance) {
                methodReferences = List.of(UnknownMethodReference.instance);
            } else {
                methodReferences = objectReference.type.getInstanceMethods()
                        .stream()
                        .filter(m -> m.getName().equals(memberAccess.name.value))
                        .filter(m -> {
                            if (m instanceof NativeMethodReference ref) {
                                return context.isMethodVisible(ref.getUnderlying());
                            } else {
                                return true;
                            }
                        })
                        .toList();
            }

            BindInvocableArgsResult<MethodReference> result = bindInvocableArguments(
                    invocation.arguments,
                    methodReferences,
                    UnknownMethodReference.instance);
            if (result.noInvocables) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        memberAccess.name,
                        objectReference.type, memberAccess.name.value);
            } else if (result.noOverload) {
                addDiagnostic(
                        BinderErrors.NoOverloadedMethods,
                        invocation.callee,
                        memberAccess.name.value, invocation.arguments.arguments.size());
            }

            BoundMethodNode methodNode = new BoundMethodNode(result.invocable, memberAccess.name.getRange());
            return new BoundMethodInvocationExpressionNode(
                    objectReference,
                    methodNode,
                    result.argumentsListNode,
                    context.releaseRefVariables(),
                    invocation.getRange());
        }*/

//        if (invocation.callee instanceof NameExpressionNode name) {
//            // function invocation
//            BoundExpressionNode boundExpr = bindNameExpression(name);
//            if (boundExpr.getNodeType() == NodeType.FUNCTION_REFERENCE) {
//                BoundFunctionReferenceNode functionReferenceNode = (BoundFunctionReferenceNode) boundExpr;
//                SStaticFunction type = functionReferenceNode.getFunctionType();
//
//                BindInvocableArgsResult<Function> result = bindInvocableArguments(
//                        invocation.arguments,
//                        List.of(functionReferenceNode.getFunction()));
//
//                if (result.noOverload) {
//                    addDiagnostic(
//                            BinderErrors.ArgumentCountMismatch,
//                            name,
//                            name.value,
//                            type.getParameters().size());
//                }
//
//                return new BoundFunctionInvocationExpression(
//                        functionReferenceNode,
//                        type.getReturnType(),
//                        result.argumentsListNode,
//                        context.releaseRefVariables(),
//                        invocation.getRange());
//            }
//
//            if (boundExpr.getNodeType() == NodeType.NAME_EXPRESSION) {
//                BoundNameExpressionNode boundName = (BoundNameExpressionNode) boundExpr;
//                if (boundName.type instanceof SGenericFunction genericFunction) {
//                    InvocableVariable invocable = new InvocableVariable(boundName.symbolRef.asVariable());
//                    BindInvocableArgsResult<InvocableVariable> result = bindInvocableArguments(
//                            invocation.arguments,
//                            List.of(invocable));
//
//                    if (result.noOverload) {
//                        addDiagnostic(
//                                BinderErrors.ArgumentCountMismatch,
//                                name,
//                                name.value,
//                                genericFunction.getParameters().size());
//                    }
//
//                    return new BoundVariableInvocationExpression(boundName, result.argumentsListNode, invocation.getRange());
//                }
//            }
//
//            addDiagnostic(
//                    BinderErrors.NotFunction,
//                    name,
//                    name.value);
//            return new BoundInvalidExpressionNode(invocation.getRange());
//        }
//
//        addDiagnostic(
//                BinderErrors.InvalidCallee,
//                invocation.callee,
//                invocation.callee.getNodeType().toString());
//        return new BoundInvalidExpressionNode(invocation.getRange());
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
            NameExpressionNode name = node.parameters.get(i);
            SType type = actualParameterTypes[i];
            SymbolRef symbolRef = context.addLocalParameter2(name.value, type, node.parameters.get(i).getRange());
            TextRange range = name.getRange();
            BoundNameExpressionNode boundName = new BoundNameExpressionNode(symbolRef, range);
            parameters.add(new BoundParameterNode(boundName, type, range));
        }

        BoundStatementNode statement;
        if (functionType.isFunction() && node.body instanceof ExpressionStatementNode expressionStatement) {
            // rewrite to return statement
            BoundExpressionNode expression = bindExpression(expressionStatement.expression);
            BoundExpressionNode converted = convert(expression, actualReturnType);
            statement = new BoundReturnStatementNode(null, converted, node.body.getRange());
        } else {
            statement = bindStatement(node.body);
        }

        List<LiftedVariable> lifted = context.getLifted();
        List<CapturedVariable> captured = context.getCaptured();

        popScope();

        return new BoundLambdaExpressionNode(
                functionType,
                parameters,
                node.arrow,
                statement,
                lifted,
                captured,
                node.getRange());
    }

    private BoundExpressionNode bindNameExpression(NameExpressionNode name) {
        SymbolRef symbolRef = getSymbol(name.value);

        if (symbolRef != null) {
            if (symbolRef.get() instanceof Function) {
                return new BoundFunctionReferenceNode(name.value, symbolRef, name.getRange());
            } else {
                return new BoundNameExpressionNode(symbolRef, name.getRange());
            }
        }

        if (context.isClassMethod()) {
            PropertyReference property = context.getClassType().getInstanceProperty(name.value);
            if (property != null) {
                return new BoundPropertyAccessExpressionNode(
                        new BoundThisExpressionNode(context.getClassType(), name.getRange().getStart()),
                        null,
                        new BoundPropertyNode(name.value, property, name.getRange()),
                        name.getRange());
            }

            List<MethodReference> methods = context.getClassType().getInstanceMethods().stream()
                    .filter(m -> m.getName().equals(name.value))
                    .toList();
            if (!methods.isEmpty()) {
                return new BoundMethodGroupExpressionNode(
                        new BoundThisExpressionNode(context.getClassType(), name.getRange().getStart()),
                        null,
                        methods,
                        new BoundUnresolvedMethodNode(name.value, name.getRange()),
                        name.getRange());
            }
        }

        Optional<Class<?>> optional = parameters.getCustomTypes().stream().filter(c -> {
            return c.getAnnotation(CustomType.class).name().equals(name.value);
        }).findFirst();
        if (optional.isPresent()) {
            SType type = SType.fromJavaType(optional.get());
            BoundCustomTypeNode typeNode = new BoundCustomTypeNode(type, name.getRange());
            return new BoundStaticReferenceExpression(typeNode, new SStaticTypeReference(type), name.getRange());
        }

        addDiagnostic(
                BinderErrors.NameDoesNotExist,
                name,
                name.value);
        return new BoundNameExpressionNode(new InvalidSymbolRef(), SUnknown.instance, name.value, name.getRange());
    }

    private BoundThisExpressionNode bindThisExpression(ThisExpressionNode expression) {
        if (context.isClassMethod()) {
            return new BoundThisExpressionNode(context.getClassType(), expression.getRange());
        } else {
            addDiagnostic(BinderErrors.ThisInvalidContext, expression);
            return new BoundThisExpressionNode(SUnknown.instance, expression.getRange());
        }
    }

    private BoundStaticReferenceExpression bindStaticReferenceExpression(StaticReferenceNode node) {
        BoundTypeNode typeNode = bindType(node.typeNode);
        return new BoundStaticReferenceExpression(typeNode, new SStaticTypeReference(typeNode.type), node.getRange());
    }

    private BoundArrayCreationExpressionNode bindArrayCreationExpression(ArrayCreationExpressionNode expression) {
        if (expression.typeNode.getNodeType() != NodeType.ARRAY_TYPE) {
            throw new InternalException();
        }

        BoundTypeNode typeNode = bindType(expression.typeNode);
        BoundExpressionNode lengthExpression = convert(bindExpression(expression.lengthExpression), SInt.instance);
        return new BoundArrayCreationExpressionNode(typeNode, lengthExpression, expression.getRange());
    }

    private BoundArrayInitializerExpressionNode bindArrayInitializerExpression(ArrayInitializerExpressionNode expression) {
        if (expression.typeNode.getNodeType() != NodeType.ARRAY_TYPE) {
            throw new InternalException();
        }

        BoundTypeNode typeNode = bindType(expression.typeNode);
        SArrayType type = (SArrayType) typeNode.type;
        List<BoundExpressionNode> items = new ArrayList<>(expression.items.size());
        for (ExpressionNode e : expression.items) {
            items.add(convert(bindExpression(e), type.getElementsType()));
        }

        return new BoundArrayInitializerExpressionNode(typeNode, items, expression.getRange());
    }

    private BoundObjectCreationExpressionNode bindObjectCreationExpressionNode(ObjectCreationExpressionNode expression) {
        BoundTypeNode typeNode = bindType(expression.typeNode);

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
                typeNode,
                result.invocable,
                result.argumentsListNode,
                expression.getRange());
    }

    private <T extends Invocable> BindInvocableArgsResult<T> bindInvocableArguments(ArgumentsListNode argumentsListNode, List<T> candidates) {
        if (candidates.size() != 1) {
            throw new InternalException();
        }
        return bindInvocableArguments(argumentsListNode, candidates, null);
    }

    private <T extends Invocable> BindInvocableArgsResult<T> bindInvocableArguments(ArgumentsListNode argumentsListNode, List<T> candidates, T unknown) {
        int argumentsSize = argumentsListNode.arguments.size();
        List<BoundExpressionNode> arguments = new ArrayList<>(argumentsSize);
        for (ExpressionNode node : argumentsListNode.arguments) {
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

        BoundArgumentsListNode boundArgumentsListNode = new BoundArgumentsListNode(arguments, argumentsListNode.getRange());
        return new BindInvocableArgsResult<>(matchedInvocable, boundArgumentsListNode, noInvocables, noOverloads);
    }

    private BoundExpressionNode bindCollectionExpression(CollectionExpressionNode collection) {
        if (collection.items.isEmpty()) {
            return new BoundEmptyCollectionExpressionNode(collection.getRange());
        }

        List<BoundExpressionNode> items = collection.items.stream().map(this::bindExpression).toList();
        SType type = items.get(0).type;
        for (int i = 1; i < items.size(); i++) {
            if (!items.get(i).type.equals(type)) {
                addDiagnostic(BinderErrors.CannotInferCollectionExpressionTypes, collection.items.get(i), type, i, items.get(i).type);
                break;
            }
        }

        return new BoundCollectionExpressionNode(new SArrayType(type), items, collection.getRange());
    }

    private BoundExpressionNode bindMemberAccessExpression(MemberAccessExpressionNode expression) {
        BoundExpressionNode callee = bindExpression(expression.callee);
        if (callee.type instanceof SStaticTypeReference staticType) {
            PropertyReference property = staticType.getUnderlying().getStaticProperties().stream()
                    .filter(p -> p.getName().equals(expression.name.value))
                    .findFirst()
                    .orElse(null);
            if (property != null) {
                return new BoundPropertyAccessExpressionNode(
                        callee,
                        expression.dot,
                        new BoundPropertyNode(expression.name.value, property, expression.name.getRange()),
                        expression.getRange());
            }

            List<MethodReference> methods = staticType.getUnderlying().getStaticMethods().stream()
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
                        callee,
                        expression.dot,
                        new BoundPropertyNode(expression.name.value, UnknownPropertyReference.instance, expression.name.getRange()),
                        expression.getRange());
            }

            return new BoundMethodGroupExpressionNode(
                    callee,
                    expression.dot,
                    methods,
                    new BoundUnresolvedMethodNode(expression.name.value, expression.name.getRange()),
                    expression.getRange());
        } else {
            PropertyReference property;
            if (callee.type == SUnknown.instance || expression.name.value.isEmpty()) {
                property = UnknownPropertyReference.instance;
            } else {
                property = callee.type.getInstanceProperty(expression.name.value);
            }
            if (property != null) {
                return new BoundPropertyAccessExpressionNode(
                        callee,
                        expression.dot,
                        new BoundPropertyNode(expression.name.value, property, expression.name.getRange()),
                        expression.getRange());
            }

            List<MethodReference> methods = callee.type.getInstanceMethods().stream()
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
                        callee,
                        expression.dot,
                        new BoundPropertyNode(expression.name.value, UnknownPropertyReference.instance, expression.name.getRange()),
                        expression.getRange());
            }

            return new BoundMethodGroupExpressionNode(
                    callee,
                    expression.dot,
                    methods,
                    new BoundUnresolvedMethodNode(expression.name.value, expression.name.getRange()),
                    expression.getRange());
        }
    }

    private BoundRefArgumentExpressionNode bindRefArgumentExpression(RefArgumentExpressionNode expression) {
        BoundExpressionNode boundExpressionNode = bindNameExpression(expression.name);
        if (boundExpressionNode.getNodeType() != NodeType.NAME_EXPRESSION) {
            addDiagnostic(BinderErrors.InvalidRefExpression, expression);
            return new BoundRefArgumentExpressionNode(
                    new BoundNameExpressionNode(new InvalidSymbolRef(), expression.name.getRange()),
                    null,
                    SUnknown.instance,
                    expression.getRange());
        }

        BoundNameExpressionNode name = (BoundNameExpressionNode) boundExpressionNode;

        SType type;
        LocalVariable holder;
        if (boundExpressionNode.type == SUnknown.instance) {
            type = SUnknown.instance;
            holder = null;
        } else {
            type = boundExpressionNode.type.getReferenceType();
            if (type == null) {
                addDiagnostic(BinderErrors.RefTypeNotSupported, expression, boundExpressionNode.type);
                return new BoundRefArgumentExpressionNode(name, null, SUnknown.instance, expression.getRange());
            }
            Variable variable = name.symbolRef.asVariable();
            holder = context.createRefVariable(variable);
        }
        return new BoundRefArgumentExpressionNode(name, holder, type, expression.getRange());
    }

    private BoundIndexExpressionNode bindIndexExpression(IndexExpressionNode indexExpression) {
        BoundExpressionNode callee = bindExpression(indexExpression.callee);
        BoundExpressionNode index = bindExpression(indexExpression.index);

        IndexOperation operation = null;
        if (callee.type.supportedIndexers().contains(index.type)) {
            operation = callee.type.index(index.type);
        } else {
            for (SType type : callee.type.supportedIndexers()) {
                CastOperation cast = SType.implicitCastTo(index.type, type);
                if (cast != null) {
                    operation = callee.type.index(type);
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

        return new BoundIndexExpressionNode(callee, index, operation, indexExpression.getRange());
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
            addDiagnostic(BinderErrors.AwaitInNonAsyncContext, node.awaitToken);
        }

        BoundExpressionNode expression = bindExpression(node.expression);
        if (expression.type == SUnknown.instance) {
            return new BoundAwaitExpressionNode(expression, SUnknown.instance, node.getRange());
        }
        if (expression.type instanceof SFuture future) {
            return new BoundAwaitExpressionNode(expression, future.getUnderlying(), node.getRange());
        } else {
            addDiagnostic(BinderErrors.CannotAwaitNonFuture, expression);
            return new BoundAwaitExpressionNode(expression, SUnknown.instance, node.getRange());
        }
    }

    private BoundInvalidMetaExpressionNode bindInvalidMetaExpression(InvalidMetaExpressionNode expression) {
        return new BoundInvalidMetaExpressionNode(expression.getRange());
    }

    private BoundMetaTypeExpressionNode bindMetaTypeExpression(MetaTypeExpressionNode meta) {
        BoundTypeNode type = bindType(meta.type);
        return new BoundMetaTypeExpressionNode(type, meta);
    }

    private BoundMetaTypeOfExpressionNode bindMetaTypeOfExpression(MetaTypeOfExpressionNode meta) {
        BoundExpressionNode expression = bindExpression(meta.expression);
        return new BoundMetaTypeOfExpressionNode(expression, meta);
    }

    private BoundInvalidExpressionNode bindInvalidExpression(InvalidExpressionNode expression) {
        return new BoundInvalidExpressionNode(List.of(), expression.getRange());
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
        if (info == null) {
            throw new InternalException();
        }

        if (info.type() == ConversionType.IDENTITY) {
            return expression;
        }

        if (info.type() == ConversionType.EMPTY_ARRAY) {
            return new BoundCollectionExpressionNode(type, List.of(), expression.getRange());
        }

        if (info.type() == ConversionType.LAMBDA_BINDING) {
            return bindUnconvertedLambda((BoundUnconvertedLambdaExpressionNode) expression, (SFunction) type);
        }

        return new BoundConversionNode(expression, info, type, expression.getRange());
    }

    private ConversionInfo getConversionInfo(BoundExpressionNode expression, SType type) {
        if (expression.type.isInstanceOf(type)) {
            return new ConversionInfo(ConversionType.IDENTITY);
        }

        CastOperation operation = SType.implicitCastTo(expression.type, type);
        if (operation != null) {
            return new ConversionInfo(ConversionType.IMPLICIT_CAST, operation);
        }

        if (expression.getNodeType() == NodeType.EMPTY_COLLECTION_EXPRESSION) {
            if (type instanceof SArrayType) {
                return new ConversionInfo(ConversionType.EMPTY_ARRAY);
            } else {
                return null;
            }
        }

        if (expression.getNodeType() == NodeType.UNCONVERTED_LAMBDA) {
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

        if (expression.getNodeType() == NodeType.FUNCTION_REFERENCE) {
            BoundFunctionReferenceNode functionReferenceNode = (BoundFunctionReferenceNode) expression;
            if (type instanceof SFunctionalInterface functionalInterface) {
                if (functionReferenceNode.getFunctionType().matches(functionalInterface)) {
                    return new ConversionInfo(ConversionType.FUNCTION_TO_INTERFACE);
                } else {
                    return null;
                }
            }
            if (type instanceof SGenericFunction genericFunction) {
                if (functionReferenceNode.getFunctionType().matches(genericFunction)) {
                    return new ConversionInfo(ConversionType.FUNCTION_TO_GENERIC);
                } else {
                    return null;
                }
            }
            return null;
        }

        if (expression.getNodeType() == NodeType.METHOD_GROUP) {
            BoundMethodGroupExpressionNode methodGroupExpressionNode = (BoundMethodGroupExpressionNode) expression;
            if (type instanceof SFunctionalInterface functionalInterface) {
                for (MethodReference method : methodGroupExpressionNode.candidates) {
                    if (method.matches(functionalInterface)) {
                        return new ConversionInfo(ConversionType.METHOD_GROUP_TO_INTERFACE, method);
                    }
                }
                return null;
            }
            if (type instanceof SGenericFunction genericFunction) {
                for (MethodReference method : methodGroupExpressionNode.candidates) {
                    if (method.matches(genericFunction)) {
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
        if (type instanceof InvalidTypeNode) {
            return new BoundInvalidTypeNode(type.getRange());
        }
        if (type instanceof VoidTypeNode) {
            return new BoundVoidTypeNode(type.getRange());
        }
        if (type instanceof PredefinedTypeNode predefined) {
            SType bound = switch (predefined.type) {
                case BOOLEAN -> SBoolean.instance;
                case INT8 -> SInt8.instance;
                case INT16 -> SInt16.instance;
                case INT -> SInt.instance;
                case INT64 -> SInt64.instance;
                case FLOAT32 -> SFloat32.instance;
                case FLOAT -> SFloat.instance;
                case STRING -> SString.instance;
                case CHAR -> SChar.instance;
            };
            return new BoundPredefinedTypeNode(bound, predefined.getRange());
        }
        if (type instanceof CustomTypeNode custom) {
            SymbolRef symbolRef = getSymbol(custom.value);
            if (symbolRef != null) {
                if (symbolRef.get() instanceof ClassSymbol) {
                    return new BoundCustomTypeNode(symbolRef.get().getType(), custom.getRange());
                } else {
                    addDiagnostic(BinderErrors.IdentifierIsNotType, type, custom.value);
                    return new BoundInvalidTypeNode(type.getRange());
                }
            }

            Optional<Class<?>> optional = parameters.getCustomTypes().stream().filter(c -> {
                return c.getAnnotation(CustomType.class).name().equals(custom.value);
            }).findFirst();
            if (optional.isPresent()) {
                return new BoundCustomTypeNode(SType.fromJavaType(optional.get()), type.getRange());
            } else {
                addDiagnostic(BinderErrors.TypeNotDefined, type, custom.value);
                return new BoundInvalidTypeNode(type.getRange());
            }
        }
        if (type instanceof ArrayTypeNode array) {
            BoundTypeNode underlying = bindType(array.underlying);
            return new BoundArrayTypeNode(underlying, array.getRange());
        }
        if (type instanceof RefTypeNode ref) {
            BoundTypeNode underlying = bindType(ref.underlying);
            return new BoundRefTypeNode(underlying, underlying.type.getReferenceType(), ref.getRange());
        }
        if (type instanceof JavaTypeNode java) {
            if (context.isJavaTypeUsageAllowed()) {
                Class<?> clazz;
                try {
                    clazz = Class.forName(java.name.value, false, ClassLoader.getSystemClassLoader());
                } catch (ClassNotFoundException e) {
                    clazz = null;
                }
                if (clazz != null) {
                    return new BoundJavaTypeNode(java.lBracket, java.name, java.rBracket, new SClassType(clazz), java.getRange());
                } else {
                    addDiagnostic(BinderErrors.JavaTypeDoesNotExist, java, java.name.value);
                    return new BoundJavaTypeNode(java.lBracket, java.name, java.rBracket, SUnknown.instance, java.getRange());
                }
            } else {
                addDiagnostic(BinderErrors.JavaTypeNotAllowed, java, context.getJavaTypeUsageError());
                return new BoundJavaTypeNode(java.lBracket, java.name, java.rBracket, SUnknown.instance, java.getRange());
            }
        }
        if (type instanceof FunctionTypeNode functionTypeNode) {
            BoundTypeNode returnTypeNode = bindType(functionTypeNode.returnTypeNode);
            List<BoundTypeNode> parameterTypeNodes = functionTypeNode.parameterTypes.stream().map(this::bindType).toList();
            SGenericFunction functionType = context.getGenericFunction(returnTypeNode.type, parameterTypeNodes.stream().map(node -> node.type).toArray(SType[]::new));
            return new BoundFunctionTypeNode(
                    functionTypeNode.open,
                    returnTypeNode,
                    parameterTypeNodes,
                    functionTypeNode.close,
                    functionType,
                    functionTypeNode.getRange());
        }
        if (type.getNodeType() == NodeType.LET_TYPE) {
            addDiagnostic(BinderErrors.LetInvalidContext, type);
            return new BoundInvalidTypeNode(type.getRange());
        }

        throw new InternalException();
    }

    private void buildDeclarationTable(CompilationUnitNode unit) {
        declarationTable = new DeclarationTable();

        // 1. process classes
        for (CompilationUnitMemberNode member : unit.members.members) {
            if (member.getNodeType() == NodeType.CLASS_DECLARATION) {
                buildClassDeclaration((ClassNode) member);
            }
        }

        // 2. process static variables and functions
        for (CompilationUnitMemberNode member : unit.members.members) {
            switch (member.getNodeType()) {
                case CLASS_DECLARATION -> {}
                case STATIC_VARIABLE -> buildStaticFieldDeclaration((StaticVariableNode) member);
                case FUNCTION -> buildFunctionDeclaration((FunctionNode) member);
                default -> throw new InternalException();
            }
        }

        // 3. process class members
        declarationTable.forEachClassDeclaration(((classNode, classDeclaration) -> {
            for (ClassMemberNode classMember : classNode.members) {
                switch (classMember.getNodeType()) {
                    case CLASS_FIELD -> buildClassFieldDeclaration(classDeclaration, (ClassFieldNode) classMember);
                    case CLASS_CONSTRUCTOR -> buildClassConstructorDeclaration(classDeclaration, (ClassConstructorNode) classMember);
                    case CLASS_METHOD -> buildClassMethodDeclaration(classDeclaration, (ClassMethodNode) classMember);
                    default -> throw new InternalException();
                }
            }
        }));
    }

    private void buildClassDeclaration(ClassNode classNode) {
        String name = classNode.name.value;
        if (!name.isEmpty() && declarationTable.hasSymbol(name)) {
            addDiagnostic(BinderErrors.SymbolAlreadyDeclared, classNode.name, name);
        }

        SDeclaredType declaredType = new SDeclaredType(name);
        ClassSymbol classSymbol = new ClassSymbol(name, declaredType, classNode.name.getRange());
        declarationTable.addClass(name, classNode, new ClassDeclaration(name, new ImmutableSymbolRef(classSymbol)));
    }

    private void buildStaticFieldDeclaration(StaticVariableNode fieldNode) {
        String name = fieldNode.name.value;
        boolean hasError = false;
        if (!name.isEmpty() && declarationTable.hasSymbol(name)) {
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
        boolean hasError = false;
        if (!name.isEmpty() && declarationTable.hasSymbol(name)) {
            hasError = true;
            addDiagnostic(BinderErrors.SymbolAlreadyDeclared, functionNode.name, name);
        }

        boolean isAsync = functionNode.modifiers.isAsync();
        BoundTypeNode returnTypeNode = bindType(functionNode.returnType);
        SType actualReturnType = isAsync ? new SFuture(returnTypeNode.type) : returnTypeNode.type;
        BoundParameterListNode parameters = bindParameterList(functionNode.parameters);
        SStaticFunction functionType = new SStaticFunction(
                actualReturnType,
                parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));
        SymbolRef symbolRef = new ImmutableSymbolRef(new Function(name, functionType, functionNode.name.getRange()));
        declarationTable.addFunction(functionNode, new FunctionDeclaration(
                name,
                symbolRef,
                isAsync,
                returnTypeNode,
                parameters,
                functionType,
                hasError));
    }

    private void buildClassFieldDeclaration(ClassDeclaration classDeclaration, ClassFieldNode classFieldNode) {
        String fieldName = classFieldNode.name.value;
        BoundTypeNode typeNode = bindType(classFieldNode.type);

        boolean hasError = false;
        if (classDeclaration.hasMember(fieldName)) {
            hasError = true;
            addDiagnostic(BinderErrors.MemberAlreadyDeclared, classFieldNode.name, fieldName);
        } else {
            classDeclaration.getDeclaredType().addField(typeNode.type, fieldName);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new ClassPropertySymbol(fieldName, typeNode.type, classFieldNode.name.getRange()));
        classDeclaration.addField(classFieldNode, new ClassFieldDeclaration(fieldName, symbolRef, typeNode, hasError));
    }

    private void buildClassConstructorDeclaration(ClassDeclaration classDeclaration, ClassConstructorNode constructorNode) {
        BoundParameterListNode parameters = bindParameterList(constructorNode.parameters);
        SMethodFunction functionType = new SMethodFunction(SVoidType.instance, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        if (classDeclaration.hasConstructor(parameters.parameters)) {
            hasError = true;
            addDiagnostic(BinderErrors.ConstructorAlreadyDeclared, constructorNode.keyword);
        } else {
            classDeclaration.getDeclaredType().addConstructor(functionType);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new ConstructorSymbol(functionType, constructorNode.keyword.getRange()));
        classDeclaration.addConstructor(constructorNode, new ClassConstructorDeclaration(symbolRef, parameters, hasError));
    }

    private void buildClassMethodDeclaration(ClassDeclaration classDeclaration, ClassMethodNode methodNode) {
        boolean isAsync = methodNode.modifiers.isAsync();
        BoundTypeNode typeNode = bindType(methodNode.type);
        SType actualReturnType = isAsync ? new SFuture(typeNode.type) : typeNode.type;
        String methodName = methodNode.name.value;
        BoundParameterListNode parameters = bindParameterList(methodNode.parameters);
        SMethodFunction functionType = new SMethodFunction(actualReturnType, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

        boolean hasError = false;
        if (classDeclaration.hasField(methodName)) {
            hasError = true;
            addDiagnostic(BinderErrors.MemberAlreadyDeclared, methodNode.name, methodName);
        } else if (classDeclaration.hasMethod(typeNode, methodName, parameters.parameters)) {
            hasError = true;
            addDiagnostic(BinderErrors.MethodAlreadyDeclared, methodNode.name);
        } else {
            classDeclaration.getDeclaredType().addMethod(functionType, methodName);
        }

        SymbolRef symbolRef = new ImmutableSymbolRef(new MethodSymbol(methodName, functionType, methodNode.name.getRange()));
        classDeclaration.addMethod(methodNode, new ClassMethodDeclaration(methodName, symbolRef, isAsync, typeNode, parameters, hasError));
    }

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

    private void pushFunctionScope(SType returnType, boolean isAsync) {
        context = context.createFunction(returnType, isAsync);
    }

    private void pushStaticFunctionScope(SType returnType, boolean isAsync) {
        context = context.createStaticFunction(returnType, isAsync);
    }

    private void pushClassScope(SDeclaredType type) {
        context = context.createClass(type);
    }

    private void pushConstructorScope() {
        context = context.createClassMethod(SVoidType.instance, false);
    }

    private void pushMethodScope(SType returnType, boolean isAsync) {
        context = context.createClassMethod(returnType, isAsync);
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
}