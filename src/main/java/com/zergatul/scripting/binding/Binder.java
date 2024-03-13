package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.ErrorCode;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.Symbol;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import com.zergatul.scripting.type.operation.UndefinedBinaryOperation;
import com.zergatul.scripting.type.operation.UndefinedUnaryOperation;

import java.util.*;

public class Binder {

    private final String code;
    private final CompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;
    private final CompilerContext context;

    public Binder(ParserOutput input, CompilerContext context) {
        this.code = input.code();
        this.unit = input.unit();
        this.diagnostics = input.diagnostics();
        this.context = context;
    }

    public BinderOutput bind() {
        return new BinderOutput(code, bindCompilationUnit(unit), diagnostics);
    }

    private BoundCompilationUnitNode bindCompilationUnit(CompilationUnitNode node) {
        return new BoundCompilationUnitNode(node.statements.stream().map(this::bindStatement).toList(), node.getRange());
    }

    private BoundStatementNode bindStatement(StatementNode statement) {
        return switch (statement.getNodeType()) {
            case ASSIGNMENT_STATEMENT -> bindAssignmentStatement((AssignmentStatementNode) statement);
            case VARIABLE_DECLARATION -> bindVariableDeclaration((VariableDeclarationNode) statement);
            case EXPRESSION_STATEMENT -> bindExpressionStatement((ExpressionStatementNode) statement);
            default -> throw new InternalException();
        };
    }

    private BoundAssignmentStatementNode bindAssignmentStatement(AssignmentStatementNode assignmentStatement) {
        throw new InternalException();
    }

    private BoundVariableDeclarationNode bindVariableDeclaration(VariableDeclarationNode variableDeclaration) {
        BoundTypeNode variableType = bindType(variableDeclaration.type);

        BoundExpressionNode expression;
        if (variableDeclaration.expression != null) {
            expression = tryCastTo(bindExpression(variableDeclaration.expression), variableType.type);
        } else {
            expression = null;
        }

        Symbol existing = context.getSymbol(variableDeclaration.name.value);
        Symbol localVar = null;
        if (existing != null) {
            addDiagnostic(
                    BinderErrors.SymbolAlreadyDeclared,
                    variableDeclaration.name,
                    variableDeclaration.name.value);
        } else {
            localVar = context.addLocalVariable(variableDeclaration.name.value, variableType.type);
        }

        BoundNameExpressionNode name = new BoundNameExpressionNode(
                localVar,
                variableType.type,
                variableDeclaration.name.value,
                variableDeclaration.name.getRange());

        return new BoundVariableDeclarationNode(variableType, name, expression, variableDeclaration.getRange());
    }

    private BoundExpressionStatementNode bindExpressionStatement(ExpressionStatementNode statement) {
        return new BoundExpressionStatementNode(bindExpression(statement.expression), statement.getRange());
    }

    private BoundExpressionNode bindExpression(ExpressionNode expression) {
        return switch (expression.getNodeType()) {
            case BOOLEAN_LITERAL -> bindBooleanLiteralExpression((BooleanLiteralExpressionNode) expression);
            case INTEGER_LITERAL -> bindIntegerLiteralExpression((IntegerLiteralExpressionNode) expression);
            case UNARY_EXPRESSION -> bindUnaryExpression((UnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> bindBinaryExpression((BinaryExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> bindConditionalExpression((ConditionalExpressionNode) expression);
            case INDEX_EXPRESSION -> bindIndexExpression((IndexExpressionNode) expression);
            case INVOCATION_EXPRESSION -> bindInvocationExpression((InvocationExpressionNode) expression);
            case NAME_EXPRESSION -> bindNameExpression((NameExpressionNode) expression);
            case MEMBER_ACCESS_EXPRESSION -> bindMemberAccessExpression((MemberAccessExpressionNode) expression);
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
        BoundBinaryOperatorNode operator;
        if (operation != null) {
            operator = new BoundBinaryOperatorNode(operation, binary.operator.getRange());
        } else {
            addDiagnostic(
                    BinderErrors.BinaryOperatorNotDefined,
                    binary,
                    binary.operator.operator.toString(),
                    left.type.toString(),
                    right.type.toString());
            operator = new BoundBinaryOperatorNode(UndefinedBinaryOperation.instance, binary.operator.getRange());
        }
        return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
    }

    private BoundBooleanLiteralExpressionNode bindBooleanLiteralExpression(BooleanLiteralExpressionNode bool) {
        return new BoundBooleanLiteralExpressionNode(bool.value, bool.getRange());
    }

    private BoundIntegerLiteralExpressionNode bindIntegerLiteralExpression(IntegerLiteralExpressionNode integer) {
        int value;
        try {
            value = Integer.parseInt(integer.value);
        } catch (NumberFormatException e) {
            value = 0;
            ErrorCode code = integer.value.charAt(0) == '-' ? BinderErrors.IntegerConstantTooSmall : BinderErrors.IntegerConstantTooLarge;
            addDiagnostic(code, integer);
        }
        return new BoundIntegerLiteralExpressionNode(value, integer.getRange());
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

    private BoundInvocationExpressionNode bindInvocationExpression(InvocationExpressionNode invocation) {
        boolean isMethodInvocation = invocation.callee instanceof MemberAccessExpressionNode;
        NameExpressionNode methodName = null;
        if (isMethodInvocation) {
            MemberAccessExpressionNode memberAccess = (MemberAccessExpressionNode) invocation.callee;
            methodName = memberAccess.name;
        }

        BoundExpressionNode callee = bindExpression(invocation.callee);
        BoundArgumentsListNode arguments = bindArgumentsList(invocation.arguments);

        MethodReference method = null;
        if (callee.type instanceof SMethodReferences methodReferences) {
            List<MethodReference> references = methodReferences
                    .getReferences()
                    .stream()
                    .filter(r -> r.getMethod().getParameterCount() == arguments.arguments.size())
                    .toList();
            if (references.isEmpty()) {
                if (isMethodInvocation) {
                    addDiagnostic(
                            BinderErrors.NoOverloadedMethods,
                            callee,
                            methodName.value, arguments.arguments.size());
                } else {
                    addDiagnostic(
                            BinderErrors.ArgumentCountMismatch,
                            callee,
                            arguments.arguments.size());
                }
            } else {
                List<ArgumentsCast> possibleArgumentsWithCasting = references
                        .stream()
                        .map(r -> {
                                List<SType> parameterTypes = r.getParameters();
                                List<UnaryOperation> casts = new ArrayList<>();
                                int count = 0;
                                for (int i = 0; i < parameterTypes.size(); i++) {
                                    SType expected = parameterTypes.get(i);
                                    SType actual = arguments.arguments.get(i).type;
                                    if (expected.equals(actual)) {
                                        casts.add(null);
                                    } else {
                                        UnaryOperation cast = actual.implicitCastTo(expected);
                                        if (cast != null) {
                                            casts.add(cast);
                                            count++;
                                        } else {
                                            return null;
                                        }
                                    }
                                }
                                return new ArgumentsCast(r, casts, count);
                        })
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingInt(ac -> ac.count))
                        .toList();
                if (possibleArgumentsWithCasting.isEmpty()) {
                    addDiagnostic(BinderErrors.CannotCastArguments, arguments);
                } else {
                    ArgumentsCast overload = possibleArgumentsWithCasting.get(0);
                    for (int i = 0; i < arguments.arguments.size(); i++) {
                        UnaryOperation cast = overload.casts.get(i);
                        if (cast != null) {
                            BoundExpressionNode expression = arguments.arguments.get(i);
                            arguments.arguments.set(i, new BoundImplicitCastExpressionNode(expression, cast, expression.getRange()));
                        }
                    }
                    method = overload.method;
                }
            }
        } else {
            if (isMethodInvocation) {
                addDiagnostic(
                        BinderErrors.NonInvocableMember,
                        methodName,
                        callee.type.toString(), methodName.value);
            } else {
                addDiagnostic(BinderErrors.FunctionExpected, callee);
            }
        }

        return new BoundInvocationExpressionNode(callee, arguments, method, invocation.getRange());
    }

    private BoundNameExpressionNode bindNameExpression(NameExpressionNode name) {
        Symbol symbol = context.getSymbol(name.value);
        if (symbol != null) {
            return new BoundNameExpressionNode(symbol, symbol.getType(), name.value, name.getRange());
        } else {
            addDiagnostic(
                    BinderErrors.NameDoesNotExist,
                    name,
                    name.value);
            return new BoundNameExpressionNode(null, SUnknown.instance, name.value, name.getRange());
        }
    }

    private BoundMemberAccessExpressionNode bindMemberAccessExpression(MemberAccessExpressionNode expression) {
        BoundExpressionNode callee =  bindExpression(expression.callee);
        if (callee.type instanceof SStaticTypeReference staticType) {
            throw new InternalException(); // TODO
        }

        List<MethodReference> methods = callee.type.getInstanceMethods(expression.name.value);
        if (methods.isEmpty()) {
            addDiagnostic(
                    BinderErrors.MemberDoesNotExist,
                    expression.name,
                    callee.type.toString(), expression.name.value);
        }

        return new BoundMemberAccessExpressionNode(
                callee,
                expression.name.value,
                new SMethodReferences(methods),
                expression.getRange());
    }

    private BoundIndexExpressionNode bindIndexExpression(IndexExpressionNode indexExpression) {
        BoundExpressionNode callee = bindExpression(indexExpression.callee);
        BoundExpressionNode index = bindExpression(indexExpression.index);
        BinaryOperation operation = callee.type.index(index.type);
        if (operation == null) {
            operation = UndefinedBinaryOperation.instance;
            addDiagnostic(
                    BinderErrors.CannotApplyIndex,
                    indexExpression,
                    callee.type.toString());
        }

        return new BoundIndexExpressionNode(callee, index, operation, indexExpression.getRange());
    }

    private BoundInvalidExpressionNode bindInvalidExpression(InvalidExpressionNode expression) {
        return new BoundInvalidExpressionNode(expression.getRange());
    }

    private BoundArgumentsListNode bindArgumentsList(ArgumentsListNode argumentsList) {
        return new BoundArgumentsListNode(argumentsList.arguments.stream().map(this::bindExpression).toList(), argumentsList.getRange());
    }

    private BoundExpressionNode tryCastTo(BoundExpressionNode expression, SType type) {
        if (expression.type.equals(type)) {
            return expression;
        }

        UnaryOperation operation = expression.type.implicitCastTo(type);
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

        UnaryOperation operation1 = expression1.type.implicitCastTo(expression2.type);
        if (operation1 != null) {
            expression1 = new BoundImplicitCastExpressionNode(expression1, operation1, expression1.getRange());
            return new ExpressionPair(expression1, expression2);
        }

        UnaryOperation operation2 = expression2.type.implicitCastTo(expression1.type);
        if (operation2 != null) {
            expression2 = new BoundImplicitCastExpressionNode(expression2, operation2, expression2.getRange());
            return new ExpressionPair(expression1, expression2);
        }

        expression1 = new BoundImplicitCastExpressionNode(expression1, UndefinedUnaryOperation.instance, expression1.getRange());
        expression2 = new BoundImplicitCastExpressionNode(expression2, UndefinedUnaryOperation.instance, expression2.getRange());
        return new ExpressionPair(false, expression1, expression2);
    }

    private BoundTypeNode bindType(TypeNode type) {
        if (type instanceof PredefinedTypeNode predefined) {
            SType bound = switch (predefined.type) {
                case BOOLEAN -> SBoolean.instance;
                case INT -> SIntType.instance;
                case FLOAT -> SFloatType.instance;
                case STRING -> SStringType.instance;
            };
            return new BoundPredefinedTypeNode(bound, predefined.getRange());
        }
        if (type instanceof ArrayTypeNode array) {
            BoundTypeNode underlying = bindType(array.underlying);
            return new BoundArrayTypeNode(underlying, array.getRange());
        }

        throw new InternalException();
    }

    private void addDiagnostic(ErrorCode code, Locatable locatable, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, locatable, parameters));
    }

    private record ExpressionPair(boolean result, BoundExpressionNode expression1, BoundExpressionNode expression2) {
        public ExpressionPair(BoundExpressionNode expression1, BoundExpressionNode expression2) {
            this(true, expression1, expression2);
        }
    }

    private record ArgumentsCast(MethodReference method, List<UnaryOperation> casts, int count) {}
}