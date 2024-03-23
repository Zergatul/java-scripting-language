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
import java.util.stream.Collectors;

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
            case FLOAT_LITERAL -> bindFloatLiteralExpression((FloatLiteralExpressionNode) expression);
            case STRING_LITERAL -> bindStringLiteralExpression((StringLiteralExpressionNode) expression);
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
        if (operation != null) {
            BoundBinaryOperatorNode operator = new BoundBinaryOperatorNode(operation, binary.operator.getRange());
            return new BoundBinaryExpressionNode(left, operator, right, binary.getRange());
        } else {
            // try implicit cast arguments to each other and see if operator is defined
            if (!left.type.equals(right.type)) {
                UnaryOperation cast = right.type.implicitCastTo(left.type);
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
        int value;
        try {
            value = Integer.parseInt(literal.value);
        } catch (NumberFormatException e) {
            value = 0;
            ErrorCode code = literal.value.charAt(0) == '-' ? BinderErrors.IntegerConstantTooSmall : BinderErrors.IntegerConstantTooLarge;
            addDiagnostic(code, literal);
        }
        return new BoundIntegerLiteralExpressionNode(value, literal.getRange());
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
        BoundArgumentsListNode arguments = bindArgumentsList(invocation.arguments);
        if (invocation.callee instanceof MemberAccessExpressionNode memberAccess) {
            // method invocation
            BoundExpressionNode objectReference = bindExpression(memberAccess.callee);
            // get methods by name
            List<MethodReference> methodReferences = objectReference.type.getInstanceMethods(memberAccess.name.value);

            MethodReference matchedMethod = UnknownMethodReference.instance;
            if (methodReferences.isEmpty()) {
                addDiagnostic(
                        BinderErrors.MemberDoesNotExist,
                        memberAccess.name,
                        objectReference.type.toString(), memberAccess.name.value);
            } else {
                // filter by arguments count
                methodReferences = methodReferences
                        .stream()
                        .filter(r -> r == UnknownMethodReference.instance || r.getParameters().size() == arguments.arguments.size())
                        .toList();

                if (methodReferences.isEmpty()) {
                    addDiagnostic(
                            BinderErrors.NoOverloadedMethods,
                            invocation.callee,
                            memberAccess.name.value, arguments.arguments.size());
                } else {
                    // filter/sort by types with upcasting
                    List<ArgumentsCast> possibleArgumentsWithCasting = methodReferences
                            .stream()
                            .map(r -> {
                                if (r == UnknownMethodReference.instance) {
                                    return new ArgumentsCast(r, Collections.nCopies(arguments.arguments.size(), null), 0);
                                }

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
                        matchedMethod = overload.method;
                    }
                }
            }

            return new BoundMethodInvocationExpressionNode(objectReference, matchedMethod, arguments, invocation.getRange());
        }
        if (invocation.callee instanceof NameExpressionNode name) {
            // function invocation
            throw new InternalException(); // TODO
        }

        throw new InternalException();

        /*boolean isMethodInvocation = invocation.callee instanceof MemberAccessExpressionNode;
        NameExpressionNode methodName = null;
        if (isMethodInvocation) {
            MemberAccessExpressionNode memberAccess = (MemberAccessExpressionNode) invocation.callee;
            methodName = memberAccess.name;
        }

        BoundExpressionNode callee = bindExpression(invocation.callee);
        BoundArgumentsListNode arguments = bindArgumentsList(invocation.arguments);

        MethodReference method = null;
        if (callee.type instanceof SMethodsHolder methodReferences) {
            List<MethodReference> references = methodReferences.getMethods()
                    .stream()
                    .filter(r -> r == UnknownMethodReference.instance || r.getParameters().size() == arguments.arguments.size())
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
                                if (r == UnknownMethodReference.instance) {
                                    return new ArgumentsCast(r, Collections.nCopies(arguments.arguments.size(), null), 0);
                                }

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

        if (method == null) {
            return new BoundInvocationExpressionNode(callee, arguments, invocation.getRange());
        } else {
            return new BoundInvocationExpressionNode(callee, arguments, method, invocation.getRange());
        }*/
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

    private BoundPropertyAccessExpressionNode bindMemberAccessExpression(MemberAccessExpressionNode expression) {
        BoundExpressionNode callee = bindExpression(expression.callee);
        if (callee.type instanceof SStaticTypeReference staticType) {
            throw new InternalException(); // TODO
        }

        PropertyReference property = callee.type.getInstanceProperty(expression.name.value);
        if (property == null) {
            addDiagnostic(
                    BinderErrors.MemberDoesNotExist,
                    expression.name,
                    callee.type.toString(), expression.name.value);
            property = UnknownPropertyReference.instance;
        }

        // TODO
        /*if (!property.canGet()) {

        }*/

        return new BoundPropertyAccessExpressionNode(
                callee,
                expression.name.value,
                property,
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
        List<BoundExpressionNode> arguments = argumentsList.arguments
                .stream()
                .map(this::bindExpression)
                .collect(Collectors.toCollection(ArrayList::new));
        return new BoundArgumentsListNode(arguments, argumentsList.getRange());
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