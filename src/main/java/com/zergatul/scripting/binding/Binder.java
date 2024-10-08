package com.zergatul.scripting.binding;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.*;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.NodeType;
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
        List<BoundVariableDeclarationNode> variables = node.variables.variables.stream().map(n -> bindVariableDeclaration(n, true)).toList();
        List<BoundFunctionNode> functions = bindFunctions(unit.functions.functions);
        pushFunctionScope(parameters.getReturnType());
        parameters.addFunctionalInterfaceParameters(context);
        BoundStatementsListNode statements = bindStatementList(node.statements);
        popScope();
        return new BoundCompilationUnitNode(
                new BoundStaticVariablesListNode(variables, node.variables.getRange()),
                new BoundFunctionsListNode(functions, node.functions.getRange()),
                statements,
                node.getRange());
    }

    private List<BoundFunctionNode> bindFunctions(List<FunctionNode> nodes) {
        // handle forward declaration
        List<BoundTypeNode> returnTypes = new ArrayList<>(nodes.size());
        List<CompilerContext> contexts = new ArrayList<>(nodes.size());
        List<BoundParameterListNode> parametersList = new ArrayList<>(nodes.size());
        List<Function> symbols = new ArrayList<>(nodes.size());
        for (FunctionNode node : nodes) {
            boolean isAsync = node.asyncToken != null;
            BoundTypeNode returnType = bindType(node.returnType);
            SType actualReturnType = isAsync ? new SFuture(returnType.type) : returnType.type;

            pushStaticFunctionScope(returnType.type);
            contexts.add(context);
            BoundParameterListNode parameters = bindParameterList(node.parameters);
            SFunction type = new SFunction(actualReturnType, parameters.parameters.stream().map(pn -> new MethodParameter(pn.getName().value, pn.getType())).toArray(MethodParameter[]::new));

            String identifier = node.name.value;
            Symbol existing = context.getSymbol(identifier);
            Function symbol;
            if (existing != null) {
                symbol = new Function(null, type, node.getRange());
                addDiagnostic(
                        BinderErrors.SymbolAlreadyDeclared,
                        node.name,
                        node.name.value);
            } else {
                symbol = new Function(identifier, type, node.getRange());
                context.getParent().addFunction(symbol);
            }

            popScope();

            returnTypes.add(returnType);
            parametersList.add(parameters);
            symbols.add(symbol);
        }

        // bind function bodies
        List<BoundFunctionNode> functions = new ArrayList<>(nodes.size());
        CompilerContext old = context;
        for (int i = 0; i < nodes.size(); i++) {
            context = contexts.get(i);
            FunctionNode node = nodes.get(i);
            BoundNameExpressionNode name = new BoundNameExpressionNode(symbols.get(i), node.name.getRange());
            BoundBlockStatementNode block = bindBlockStatement(node.body);

            SType returnType = returnTypes.get(i).type;
            if (returnType != SVoidType.instance && returnType != SUnknown.instance) {
                if (!new ReturnPathsVerifier().verify(block)) {
                    addDiagnostic(BinderErrors.NotAllPathReturnValue, block);
                }
            }

            functions.add(new BoundFunctionNode(node.asyncToken != null, returnTypes.get(i), name, parametersList.get(i), block, node.getRange()));
        }
        context = old;
        return functions;
    }

    private BoundParameterListNode bindParameterList(ParameterListNode node) {
        List<BoundParameterNode> parameters = new ArrayList<>(node.parameters.size());
        for (ParameterNode parameter : node.parameters) {
            BoundTypeNode type = bindType(parameter.getType());
            LocalVariable variable = type instanceof BoundRefTypeNode ref ?
                    context.addLocalRefParameter(parameter.getName().value, (SReference) ref.type, ref.underlying.type, parameter.getRange()) :
                    context.addLocalParameter(parameter.getName().value, type.type, parameter.getRange());
            BoundNameExpressionNode name = new BoundNameExpressionNode(variable, parameter.getName().getRange());
            parameters.add(new BoundParameterNode(name, type, parameter.getRange()));
        }
        return new BoundParameterListNode(parameters, node.getRange());
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
            right = tryCastTo(right, left.type);
            return new BoundAssignmentStatementNode(left, operator, right, statement.getRange());
        }

        BinaryOperation operation = left.type.binary(operator.operator.getBinaryOperator(), right.type);
        if (operation == null) {
            // try implicit cast right to left
            if (!left.type.equals(right.type)) {
                CastOperation cast = right.type.implicitCastTo(left.type);
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
        return bindVariableDeclaration(variableDeclaration, false);
    }

    private BoundVariableDeclarationNode bindVariableDeclaration(VariableDeclarationNode variableDeclaration, boolean isStatic) {
        BoundTypeNode variableType = bindType(variableDeclaration.type);

        BoundExpressionNode expression;
        if (variableDeclaration.expression != null) {
            expression = tryCastTo(bindExpression(variableDeclaration.expression), variableType.type);
        } else {
            expression = null;
        }

        Symbol existing = context.getSymbol(variableDeclaration.name.value);
        Symbol variable = null;
        if (existing != null) {
            addDiagnostic(
                    BinderErrors.SymbolAlreadyDeclared,
                    variableDeclaration.name,
                    variableDeclaration.name.value);
        } else {
            if (isStatic) {
                StaticVariable staticVariable = new DeclaredStaticVariable(variableDeclaration.name.value, variableType.type, variableDeclaration.getRange());
                context.addStaticVariable(staticVariable);
                variable = staticVariable;
            } else {
                variable = context.addLocalVariable(variableDeclaration.name.value, variableType.type, variableDeclaration.getRange());
            }
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(
                variable,
                variableType.type,
                variableDeclaration.name.value,
                variableDeclaration.name.getRange());

        return new BoundVariableDeclarationNode(variableType, name, expression, variableDeclaration.getRange());
    }

    private BoundExpressionStatementNode bindExpressionStatement(ExpressionStatementNode statement) {
        return new BoundExpressionStatementNode(bindExpression(statement.expression), statement.getRange());
    }

    private BoundIfStatementNode bindIfStatement(IfStatementNode statement) {
        BoundExpressionNode condition = tryCastTo(bindExpression(statement.condition), SBoolean.instance);
        BoundStatementNode thenStatement = bindStatement(statement.thenStatement);
        BoundStatementNode elseStatement = statement.elseStatement == null ? null : bindStatement(statement.elseStatement);
        return new BoundIfStatementNode(statement.lParen, statement.rParen, condition, thenStatement, elseStatement, statement.getRange());
    }

    private BoundReturnStatementNode bindReturnStatement(ReturnStatementNode statement) {
        if (statement.expression == null) {
            if (context.getReturnType() == SVoidType.instance) {
                return new BoundReturnStatementNode(null, statement.getRange());
            } else {
                addDiagnostic(
                        BinderErrors.EmptyReturnStatement,
                        statement);
                return new BoundReturnStatementNode(new BoundInvalidExpressionNode(statement.getRange().subRange(6)), statement.getRange());
            }
        } else {
            BoundExpressionNode expression = bindExpression(statement.expression);
            SType actual = expression.type;
            SType expected = context.getReturnType();
            if (!actual.equals(expected)) {
                CastOperation cast = actual.implicitCastTo(expected);
                if (cast == null) {
                    addDiagnostic(
                            BinderErrors.CannotImplicitlyConvert,
                            statement.expression,
                            actual,
                            expected);
                } else {
                    expression = new BoundImplicitCastExpressionNode(expression, cast, expression.getRange());
                }
            }
            return new BoundReturnStatementNode(expression, statement.getRange());
        }
    }

    private BoundForLoopStatementNode bindForLoopStatement(ForLoopStatementNode statement) {
        pushScope();
        BoundStatementNode init = statement.init != null ? bindStatement(statement.init) : null;

        BoundExpressionNode condition;
        if (statement.condition != null) {
            condition = tryCastTo(bindExpression(statement.condition), SBoolean.instance);
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

        return new BoundForLoopStatementNode(init, condition, update, body, statement.getRange());
    }

    private BoundForEachLoopStatementNode bindForEachLoopStatement(ForEachLoopStatementNode statement) {
        pushScope();

        BoundTypeNode variableType = bindType(statement.typeNode);

        Symbol existing = context.getSymbol(statement.name.value);
        BoundNameExpressionNode name = null;
        if (existing != null) {
            addDiagnostic(
                    BinderErrors.SymbolAlreadyDeclared,
                    statement.name,
                    statement.name.value);
        } else {
            LocalVariable variable = context.addLocalVariable(statement.name.value, variableType.type, TextRange.combine(statement.typeNode, statement.name));
            name = new BoundNameExpressionNode(variable, statement.name.getRange());
        }

        LocalVariable index = context.addLocalVariable(null, SInt.instance, null);
        LocalVariable length = context.addLocalVariable(null, SInt.instance, null);

        BoundExpressionNode iterable = bindExpression(statement.iterable);
        if (iterable.type instanceof SArrayType arrayType) {
            if (!arrayType.getElementsType().equals(variableType.type)) {
                addDiagnostic(BinderErrors.ForEachTypesNotMatch, statement.typeNode);
            }
        } else {
            addDiagnostic(BinderErrors.CannotIterate, iterable, iterable.type.toString());
        }

        context.setBreak(v -> {});
        context.setContinue(v -> {});
        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundForEachLoopStatementNode(
                variableType, name, iterable, body,
                index, length,
                statement.getRange());
    }

    private BoundWhileLoopStatementNode bindWhileLoopStatement(WhileLoopStatementNode statement) {
        pushScope();

        BoundExpressionNode condition = tryCastTo(bindExpression(statement.condition), SBoolean.instance);
        if (condition.type != SBoolean.instance) {
            addDiagnostic(BinderErrors.CannotImplicitlyConvert, condition, condition.type.toString(), SBoolean.instance.toString());
        }

        context.setBreak(v -> {});
        context.setContinue(v -> {});
        BoundStatementNode body = bindStatement(statement.body);

        popScope();

        return new BoundWhileLoopStatementNode(condition, body, statement.getRange());
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
            case UNARY_EXPRESSION -> bindUnaryExpression((UnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> bindBinaryExpression((BinaryExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> bindConditionalExpression((ConditionalExpressionNode) expression);
            case INDEX_EXPRESSION -> bindIndexExpression((IndexExpressionNode) expression);
            case INVOCATION_EXPRESSION -> bindInvocationExpression((InvocationExpressionNode) expression);
            case NAME_EXPRESSION -> bindNameExpression((NameExpressionNode) expression);
            case STATIC_REFERENCE -> bindStaticReferenceExpression((StaticReferenceNode) expression);
            case MEMBER_ACCESS_EXPRESSION -> bindMemberAccessExpression((MemberAccessExpressionNode) expression);
            case REF_ARGUMENT_EXPRESSION -> bindRefArgumentExpression((RefArgumentExpressionNode) expression);
            case NEW_EXPRESSION -> bindNewExpression((NewExpressionNode) expression);
            case LAMBDA_EXPRESSION -> bindLambdaExpression((LambdaExpressionNode) expression);
            case AWAIT_EXPRESSION -> bindAwaitExpression((AwaitExpressionNode) expression);
            case INVALID_EXPRESSION -> bindInvalidExpression((InvalidExpressionNode) expression);
            default -> throw new InternalException();
        };
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
                CastOperation cast = right.type.implicitCastTo(left.type);
                if (cast != null) {
                    operation = left.type.binary(binary.operator.operator, left.type);
                    if (operation != null) {
                        right = new BoundImplicitCastExpressionNode(right, cast, right.getRange());
                        BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(operation, binary.operator.getRange());
                        return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
                    }
                }

                cast = left.type.implicitCastTo(right.type);
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
        String value = literal.value;
        int begin = 0;
        int end = value.length();
        if (value.charAt(0) == '"') {
            begin++;
        }
        if (value.charAt(value.length() - 1) == '"') {
            end--;
        }

        StringBuilder builder = new StringBuilder(value.length());
        int index = begin;
        while (index < end) {
            if (value.charAt(index) == '\\') {
                index++;
                if (index < end) {
                    builder.append(switch (value.charAt(index)) {
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        case 'b' -> '\b';
                        case 'r' -> '\r';
                        case 'f' -> '\f';
                        default -> value.charAt(index);
                    });
                }
            } else {
                builder.append(value.charAt(index));
            }
            index++;
        }

        return new BoundStringLiteralExpressionNode(builder.toString(), literal.getRange());
    }

    private BoundCharLiteralExpressionNode bindCharLiteralExpression(CharLiteralExpressionNode literal) {
        String value = literal.value;
        int begin = 0;
        int end = value.length();
        if (value.charAt(0) == '\'') {
            begin++;
        }
        if (value.charAt(value.length() - 1) == '\'') {
            end--;
        }

        value = value.substring(begin, end);

        return new BoundCharLiteralExpressionNode(switch (value.length()) {
            case 0 -> {
                addDiagnostic(BinderErrors.EmptyCharLiteral, literal);
                yield (char) 0;
            }
            case 1 -> value.charAt(0);
            case 2 -> {
                if (value.charAt(0) == '\\') {
                    yield switch (value.charAt(1)) {
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        case 'b' -> '\b';
                        case 'r' -> '\r';
                        case 'f' -> '\f';
                        default -> value.charAt(1);
                    };
                } else {
                    addDiagnostic(BinderErrors.TooManyCharsInCharLiteral, literal);
                    yield (char) 0;
                }
            }
            default -> {
                addDiagnostic(BinderErrors.TooManyCharsInCharLiteral, literal);
                yield (char) 0;
            }
        }, literal.getRange());
    }

    private BoundConditionalExpressionNode bindConditionalExpression(ConditionalExpressionNode expression) {
        BoundExpressionNode condition = tryCastTo(bindExpression(expression.condition), SBoolean.instance);
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
        int argumentsSize = invocation.arguments.arguments.size();
        List<PreBoundArgument> arguments = new ArrayList<>(argumentsSize);
        for (ExpressionNode node : invocation.arguments.arguments) {
            if (node.getNodeType() == NodeType.LAMBDA_EXPRESSION) {
                arguments.add(new PreBoundArgument((LambdaExpressionNode) node));
            } else {
                arguments.add(new PreBoundArgument(bindExpression(node)));
            }
        }

        if (invocation.callee instanceof MemberAccessExpressionNode memberAccess) {
            // method invocation
            BoundExpressionNode objectReference = bindExpression(memberAccess.callee);
            // get methods by name
            List<MethodReference> methodReferences = objectReference.type.getInstanceMethods()
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

            if (objectReference.type == SUnknown.instance) {
                methodReferences = List.of(UnknownMethodReference.instance);
            }

            boolean invalidArguments = false;

            MethodReference matchedMethod = UnknownMethodReference.instance;
            if (methodReferences.isEmpty()) {
                invalidArguments = true;
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        memberAccess.name,
                        objectReference.type.toString(), memberAccess.name.value);
            } else if (methodReferences.size() == 1 && methodReferences.get(0) == UnknownMethodReference.instance) {
                invalidArguments = true;
            } else {
                // filter by arguments count
                methodReferences = methodReferences
                        .stream()
                        .filter(r -> r == UnknownMethodReference.instance || r.getParameters().size() == argumentsSize)
                        .toList();

                if (methodReferences.isEmpty()) {
                    invalidArguments = true;
                    addDiagnostic(
                            BinderErrors.NoOverloadedMethods,
                            invocation.callee,
                            memberAccess.name.value, argumentsSize);
                } else {
                    // filter/sort by types with upcasting
                    List<ArgumentsCast> possibleArgumentsWithCasting = methodReferences
                            .stream()
                            .map(r -> {
                                if (r == UnknownMethodReference.instance) {
                                    return new ArgumentsCast(r, Collections.nCopies(argumentsSize, null), 0);
                                }

                                List<SType> parameterTypes = r.getParameterTypes();
                                List<CastOperation> casts = new ArrayList<>();
                                int count = 0;
                                for (int i = 0; i < parameterTypes.size(); i++) {
                                    SType expected = parameterTypes.get(i);
                                    PreBoundArgument argument = arguments.get(i);
                                    if (argument.canMatch(expected)) {
                                        casts.add(null);
                                    } else if (argument.hasExpression()) {
                                        CastOperation cast = argument.expression.type.implicitCastTo(expected);
                                        if (cast != null) {
                                            casts.add(cast);
                                            count++;
                                        } else {
                                            return null;
                                        }
                                    } else {
                                        return null;
                                    }
                                }
                                return new ArgumentsCast(r, casts, count);
                            })
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(ac -> ac.count))
                            .toList();
                    if (possibleArgumentsWithCasting.isEmpty()) {
                        addDiagnostic(BinderErrors.CannotCastArguments, invocation.arguments);
                        invalidArguments = true;
                    } else {
                        ArgumentsCast overload = possibleArgumentsWithCasting.get(0);
                        for (int i = 0; i < argumentsSize; i++) {
                            PreBoundArgument argument = arguments.get(i);
                            if (argument.hasLambda()) {
                                arguments.set(i, new PreBoundArgument(bindLambdaExpression(
                                        argument.lambda,
                                        (SFunctionalInterface) overload.method.getParameterTypes().get(i))));
                            } else {
                                CastOperation cast = overload.casts.get(i);
                                if (cast != null) {
                                    if (cast instanceof SFunction.FunctionToLambdaOperation) {
                                        if (!(argument.expression instanceof BoundNameExpressionNode name)) {
                                            throw new InternalException();
                                        }
                                        arguments.set(i, new PreBoundArgument(new BoundFunctionAsLambdaExpressionNode(
                                                overload.method.getParameterTypes().get(i),
                                                name,
                                                name.getRange())));
                                    } else {
                                        arguments.set(i, new PreBoundArgument(new BoundImplicitCastExpressionNode(
                                                argument.expression,
                                                cast,
                                                argument.expression.getRange())));
                                    }
                                }
                            }
                        }
                        matchedMethod = overload.method;
                    }
                }
            }

            if (!invalidArguments && arguments.stream().anyMatch(PreBoundArgument::hasLambda)) {
                throw new InternalException();
            }

            BoundMethodNode methodNode = new BoundMethodNode(matchedMethod, memberAccess.name.getRange());
            BoundArgumentsListNode argumentsNode = new BoundArgumentsListNode(
                    arguments.stream().map(a -> {
                        return a.expression == null ? new BoundInvalidExpressionNode(a.lambda.getRange()) : a.expression;
                    }).toList(),
                    invocation.arguments.getRange());
            return new BoundMethodInvocationExpressionNode(objectReference, methodNode, argumentsNode, context.releaseRefVariables(), invocation.getRange());
        }

        if (invocation.callee instanceof NameExpressionNode name) {
            // function invocation
            BoundNameExpressionNode boundName = bindNameExpression(name);
            if (boundName.symbol instanceof Function function) {
                SFunction type = function.getFunctionType();
                if (argumentsSize != type.getParameters().size()) {
                    addDiagnostic(
                            BinderErrors.ArgumentCountMismatch,
                            name,
                            name.value,
                            type.getParameters().size());
                    return new BoundInvalidExpressionNode(invocation.getRange());
                }

                List<SType> parameterTypes = type.getParameterTypes();
                for (int i = 0; i < parameterTypes.size(); i++) {
                    SType expected = parameterTypes.get(i);
                    PreBoundArgument argument = arguments.get(i);
                    if (argument.canMatch(expected)) {
                        if (argument.hasLambda()) {
                            arguments.set(i, new PreBoundArgument(bindLambdaExpression(
                                    argument.lambda,
                                    (SFunctionalInterface) expected)));
                        }
                    } else if (argument.hasExpression()) {
                        CastOperation cast = argument.expression.type.implicitCastTo(expected);
                        if (cast != null) {
                            BoundExpressionNode expression = argument.expression;
                            arguments.set(i, new PreBoundArgument(
                                    new BoundImplicitCastExpressionNode(expression, cast, expression.getRange())));
                        } else {
                            addDiagnostic(
                                    BinderErrors.CannotCastArgument,
                                    argument.expression,
                                    i,
                                    argument.expression.type,
                                    expected);
                            return new BoundInvalidExpressionNode(invocation.getRange());
                        }
                    } else {
                        addDiagnostic(
                                BinderErrors.CannotCastArgument,
                                argument.expression,
                                i,
                                "Lambda<?>",
                                expected);
                        return new BoundInvalidExpressionNode(invocation.getRange());
                    }
                }

                BoundArgumentsListNode argumentsNode = new BoundArgumentsListNode(
                        arguments.stream().map(a -> a.expression).toList(),
                        invocation.arguments.getRange());

                return new BoundFunctionInvocationExpression(boundName, type.getReturnType(), argumentsNode, context.releaseRefVariables(), invocation.getRange());
            } else {
                addDiagnostic(
                        BinderErrors.NotFunction,
                        name,
                        name.value);
                return new BoundInvalidExpressionNode(invocation.getRange());
            }
        }

        addDiagnostic(
                BinderErrors.InvalidCallee,
                invocation.callee,
                invocation.callee.getNodeType().toString());
        return new BoundInvalidExpressionNode(invocation.getRange());
    }

    private BoundExpressionNode bindLambdaExpression(LambdaExpressionNode node, SFunctionalInterface lambdaType) {
        int parametersCount = lambdaType.getActualParameters().length;
        if (node.parameters.size() != parametersCount) {
            throw new InternalException("Lambda parameters count mismatch.");
        }

        pushFunctionScope(lambdaType.getActualReturnType());

        for (int i = 0; i < parametersCount; i++) {
            // parameters type will be Object due to type erasure
            context.addLocalVariable(null, SType.fromJavaType(Object.class), null);
        }

        List<BoundParameterNode> parameters = new ArrayList<>();
        for (int i = 0; i < parametersCount; i++) {
            NameExpressionNode name = node.parameters.get(i);
            SType type = lambdaType.getActualParameters()[i];
            Variable variable = context.addLocalVariable(name.value, type, node.parameters.get(i).getRange());
            TextRange range = name.getRange();
            BoundNameExpressionNode boundName = new BoundNameExpressionNode(variable, range);
            parameters.add(new BoundParameterNode(boundName, type, range));
        }

        BoundStatementNode statement;
        if (lambdaType.isFunction() && node.body instanceof ExpressionStatementNode expressionStatement) {
            // rewrite to return statement
            BoundExpressionNode expression = bindExpression(expressionStatement.expression);
            SType actual = expression.type;
            SType expected = lambdaType.getActualReturnType();
            if (!actual.equals(expected)) {
                CastOperation cast = actual.implicitCastTo(expected);
                if (cast == null) {
                    addDiagnostic(
                            BinderErrors.CannotImplicitlyConvert,
                            expression,
                            actual,
                            expected);
                } else {
                    expression = new BoundImplicitCastExpressionNode(expression, cast, expression.getRange());
                }
            }
            statement = new BoundReturnStatementNode(expression, node.body.getRange());
        } else {
            statement = bindStatement(node.body);
        }

        List<LiftedVariable> lifted = context.getLifted();
        List<CapturedVariable> captured = context.getCaptured();

        popScope();

        return new BoundLambdaExpressionNode(
                lambdaType,
                parameters,
                statement,
                lifted,
                captured,
                node.getRange());
    }

    private BoundNameExpressionNode bindNameExpression(NameExpressionNode name) {
        Symbol symbol = context.getSymbol(name.value);
        if (symbol != null) {
            return new BoundNameExpressionNode(symbol, name.getRange());
        } else {
            addDiagnostic(
                    BinderErrors.NameDoesNotExist,
                    name,
                    name.value);
            return new BoundNameExpressionNode(null, SUnknown.instance, name.value, name.getRange());
        }
    }

    private BoundStaticReferenceExpression bindStaticReferenceExpression(StaticReferenceNode node) {
        return new BoundStaticReferenceExpression(switch (node.typeReference) {
            case BOOLEAN -> SBoolean.staticRef;
            case INT -> SInt.staticRef;
            case INT64 -> SInt64.staticRef;
            case CHAR -> SChar.staticRef;
            case FLOAT -> SFloat.staticRef;
            case STRING -> SString.staticRef;
        }, node.getRange());
    }

    private BoundNewExpressionNode bindNewExpression(NewExpressionNode expression) {
        BoundTypeNode typeNode = bindType(expression.typeNode);

        BoundExpressionNode lengthExpression = null;
        if (expression.lengthExpression != null) {
            lengthExpression = tryCastTo(bindExpression(expression.lengthExpression), SInt.instance);
        }

        List<BoundExpressionNode> items = null;
        if (expression.items != null) {
            items = new ArrayList<>(expression.items.size());
            for (ExpressionNode e : expression.items) {
                items.add(bindExpression(e));
            }
        }

        if (typeNode.getNodeType() == NodeType.ARRAY_TYPE) {
            if (lengthExpression == null ^ items == null) {
                SArrayType arrayType = (SArrayType) typeNode.type;
                SType underlying = arrayType.getElementsType();
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        items.set(i, tryCastTo(items.get(i), underlying));
                    }
                }
            } else {
                addDiagnostic(BinderErrors.InvalidArrayCreation, expression);
            }
        } else {
            addDiagnostic(BinderErrors.NewSupportArraysOnly, expression);
        }

        return new BoundNewExpressionNode(typeNode, lengthExpression, items, expression.getRange());
    }

    private BoundPropertyAccessExpressionNode bindMemberAccessExpression(MemberAccessExpressionNode expression) {
        BoundExpressionNode callee = bindExpression(expression.callee);
        if (callee.type instanceof SStaticTypeReference staticType) {
            List<PropertyReference> properties = staticType.getUnderlying().getStaticProperties();
            PropertyReference property = properties.stream().filter(p -> p.getName().equals(expression.name.value)).findFirst().orElse(null);
            if (property == null) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        expression.name,
                        callee.type.toString(), expression.name.value);
                property = UnknownPropertyReference.instance;
            }

            return new BoundPropertyAccessExpressionNode(
                    callee,
                    new BoundPropertyNode(expression.name.value, property, expression.name.getRange()),
                    expression.getRange());

        } else {
            PropertyReference property = callee.type.getInstanceProperty(expression.name.value);
            if (property == null) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        expression.name,
                        callee.type.toString(), expression.name.value);
                property = UnknownPropertyReference.instance;
            }

            return new BoundPropertyAccessExpressionNode(
                    callee,
                    new BoundPropertyNode(expression.name.value, property, expression.name.getRange()),
                    expression.getRange());
        }
    }

    private BoundRefArgumentExpressionNode bindRefArgumentExpression(RefArgumentExpressionNode expression) {
        BoundNameExpressionNode name = bindNameExpression(expression.name);
        SType type;
        LocalVariable holder;
        if (name.type == SUnknown.instance) {
            type = SUnknown.instance;
            holder = null;
        } else {
            type = name.type.getReferenceType();
            Variable variable = (Variable) name.symbol;
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
                CastOperation cast = index.type.implicitCastTo(type);
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
        addDiagnostic(BinderErrors.LambdaIsInvalidInCurrentContext, expression);
        return new BoundInvalidExpressionNode(expression.getRange());
    }

    private BoundAwaitExpressionNode bindAwaitExpression(AwaitExpressionNode node) {
        if (!parameters.isAsync()) {
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

    private BoundInvalidExpressionNode bindInvalidExpression(InvalidExpressionNode expression) {
        return new BoundInvalidExpressionNode(expression.getRange());
    }

    private BoundExpressionNode tryCastTo(BoundExpressionNode expression, SType type) {
        if (expression.type.equals(type)) {
            return expression;
        }

        CastOperation operation = expression.type.implicitCastTo(type);
        if (operation != null) {
            return new BoundImplicitCastExpressionNode(expression, operation, expression.getRange());
        } else {
            addDiagnostic(
                    BinderErrors.CannotImplicitlyConvert,
                    expression,
                    expression.type.toString(), type.toString());
            return expression;
        }
    }

    private ExpressionPair tryCastToCommon(BoundExpressionNode expression1, BoundExpressionNode expression2) {
        if (expression1.type.equals(expression2.type)) {
            return new ExpressionPair(expression1, expression2);
        }

        CastOperation operation1 = expression1.type.implicitCastTo(expression2.type);
        if (operation1 != null) {
            expression1 = new BoundImplicitCastExpressionNode(expression1, operation1, expression1.getRange());
            return new ExpressionPair(expression1, expression2);
        }

        CastOperation operation2 = expression2.type.implicitCastTo(expression1.type);
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
                case INT -> SInt.instance;
                case INT64 -> SInt64.instance;
                case FLOAT -> SFloat.instance;
                case STRING -> SString.instance;
                case CHAR -> SChar.instance;
            };
            return new BoundPredefinedTypeNode(bound, predefined.getRange());
        }
        if (type instanceof CustomTypeNode custom) {
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

        throw new InternalException();
    }

    private void pushScope() {
        context = context.createChild();
    }

    private void pushFunctionScope(SType returnType) {
        context = context.createFunction(returnType);
    }

    private void pushStaticFunctionScope(SType returnType) {
        context = context.createStaticFunction(returnType);
    }

    private void popScope() {
        context = context.getParent();
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

    private record ArgumentsCast(MethodReference method, List<CastOperation> casts, int count) {}

    private record PreBoundArgument(BoundExpressionNode expression, LambdaExpressionNode lambda) {

        public PreBoundArgument(BoundExpressionNode expression) {
            this(expression, null);
        }

        public PreBoundArgument(LambdaExpressionNode lambda) {
            this(null, lambda);
        }

        public boolean canMatch(SType expected) {
            if (expression != null) {
                return expression.type.equals(expected);
            } else {
                if (expected instanceof SFunctionalInterface abstractFunction) {
                    LambdaAnalyzer analyzer = new LambdaAnalyzer();
                    if (abstractFunction.isFunction()) {
                        if (!analyzer.canBeFunction(lambda)) {
                            return false;
                        }
                    } else {
                        if (!analyzer.canBeAction(lambda)) {
                            return false;
                        }
                    }

                    SType[] parameters1 = abstractFunction.getActualParameters();
                    List<NameExpressionNode> parameters2 = lambda.parameters;
                    return parameters1.length == parameters2.size();
                } else {
                    return false;
                }
            }
        }

        public boolean hasExpression() {
            return expression != null;
        }

        public boolean hasLambda() {
            return lambda != null;
        }
    }
}