package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.ErrorCode;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.nodes.*;

import java.util.List;

public class ContextualLambdaChecker {

    private final BoundCompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;

    public ContextualLambdaChecker(BoundCompilationUnitNode unit, List<DiagnosticMessage> diagnostics) {
        this.unit = unit;
        this.diagnostics = diagnostics;
    }

    public void check() {
        checkCompilationUnit(unit);
    }

    private void checkCompilationUnit(BoundCompilationUnitNode node) {
        node.statements.forEach(this::checkStatement);
    }

    private void checkStatement(BoundStatementNode node) {
        switch (node.getNodeType()) {
            case ASSIGNMENT_STATEMENT -> checkAssignmentStatement((BoundAssignmentStatementNode) node);
            case BLOCK_STATEMENT -> checkBlockStatement((BoundBlockStatementNode) node);
            case VARIABLE_DECLARATION -> checkVariableDeclaration((BoundVariableDeclarationNode) node);
            case EXPRESSION_STATEMENT -> checkExpressionStatement((BoundExpressionStatementNode) node);
            case IF_STATEMENT -> checkIfStatement((BoundIfStatementNode) node);
            case RETURN_STATEMENT -> checkReturnStatement((BoundReturnStatementNode) node);
            case FOR_LOOP_STATEMENT -> checkForLoopStatement((BoundForLoopStatementNode) node);
            case FOREACH_LOOP_STATEMENT -> checkForEachLoopStatement((BoundForEachLoopStatementNode) node);
            case INCREMENT_STATEMENT, DECREMENT_STATEMENT -> checkPostfixStatement((BoundPostfixStatementNode) node);
            case BREAK_STATEMENT, CONTINUE_STATEMENT, INVALID_STATEMENT, EMPTY_STATEMENT -> {}
            default -> throw new InternalException();
        }
    }

    private void checkAssignmentStatement(BoundAssignmentStatementNode node) {
        checkExpression(node.left);
        checkExpression(node.right);
    }

    private void checkBlockStatement(BoundBlockStatementNode node) {
        node.statements.forEach(this::checkStatement);
    }

    private void checkVariableDeclaration(BoundVariableDeclarationNode node) {
        if (node.expression != null) {
            checkExpression(node.expression);
        }
    }

    private void checkExpressionStatement(BoundExpressionStatementNode node) {
        checkExpression(node.expression);
    }

    private void checkIfStatement(BoundIfStatementNode node) {
        checkExpression(node.condition);
        checkStatement(node.thenStatement);
        if (node.elseStatement != null) {
            checkStatement(node.elseStatement);
        }
    }

    private void checkReturnStatement(BoundReturnStatementNode node) {
        if (node.expression != null) {
            checkExpression(node.expression);
        }
    }

    private void checkForLoopStatement(BoundForLoopStatementNode node) {
        checkStatement(node.init);
        if (node.condition != null) {
            checkExpression(node.condition);
        }
        checkStatement(node.update);
        checkStatement(node.body);
    }

    private void checkForEachLoopStatement(BoundForEachLoopStatementNode node) {
        checkExpression(node.iterable);
        checkStatement(node.body);
    }

    private void checkPostfixStatement(BoundPostfixStatementNode node) {
        checkExpression(node.expression);
    }

    private void checkExpression(BoundExpressionNode node) {
        switch (node.getNodeType()) {
            case BOOLEAN_LITERAL, STRING_LITERAL, FLOAT_LITERAL, INTEGER_LITERAL, NAME_EXPRESSION, INVALID_EXPRESSION -> {}
            case UNARY_EXPRESSION -> checkUnaryExpression((BoundUnaryExpressionNode) node);
            case BINARY_EXPRESSION -> checkBinaryExpression((BoundBinaryExpressionNode) node);
            case CONDITIONAL_EXPRESSION -> checkConditionalExpression((BoundConditionalExpressionNode) node);
            case INDEX_EXPRESSION -> checkIndexExpression((BoundIndexExpressionNode) node);
            case INVOCATION_EXPRESSION -> checkInvocationExpression((BoundInvocationExpressionNode) node);
            case METHOD_INVOCATION_EXPRESSION -> checkMethodInvocationExpression((BoundMethodInvocationExpressionNode) node);
            case PROPERTY_ACCESS_EXPRESSION -> checkPropertyAccessExpression((BoundPropertyAccessExpressionNode) node);
            case NEW_EXPRESSION -> checkNewExpression((BoundNewExpressionNode) node);
            case IMPLICIT_CAST -> checkImplicitCastExpression((BoundImplicitCastExpressionNode) node);
            case LAMBDA_EXPRESSION -> checkLambdaExpression((BoundLambdaExpressionNode) node);
            case FUNCTION_INVOCATION -> checkFunctionInvocation((BoundFunctionInvocationExpression) node);
            case CONTEXTUAL_LAMBDA_EXPRESSION -> addDiagnostic(BinderErrors.ContextualLambda, node);
            default -> throw new InternalException();
        }
    }

    private void checkUnaryExpression(BoundUnaryExpressionNode node) {
        checkExpression(node.operand);
    }

    private void checkBinaryExpression(BoundBinaryExpressionNode node) {
        checkExpression(node.left);
        checkExpression(node.right);
    }

    private void checkConditionalExpression(BoundConditionalExpressionNode node) {
        checkExpression(node.condition);
        checkExpression(node.whenTrue);
        checkExpression(node.whenFalse);
    }

    private void checkIndexExpression(BoundIndexExpressionNode node) {
        checkExpression(node.callee);
        checkExpression(node.index);
    }

    private void checkInvocationExpression(BoundInvocationExpressionNode node) {
        checkExpression(node.callee);
        node.arguments.arguments.forEach(this::checkExpression);
    }

    private void checkMethodInvocationExpression(BoundMethodInvocationExpressionNode node) {
        checkExpression(node.objectReference);
        node.arguments.arguments.forEach(this::checkExpression);
    }

    private void checkPropertyAccessExpression(BoundPropertyAccessExpressionNode node) {
        checkExpression(node.callee);
    }

    private void checkNewExpression(BoundNewExpressionNode node) {
        if (node.lengthExpression != null) {
            checkExpression(node.lengthExpression);
        }
        if (node.items != null) {
            node.items.forEach(this::checkExpression);
        }
    }

    private void checkImplicitCastExpression(BoundImplicitCastExpressionNode node) {
        checkExpression(node.operand);
    }

    private void checkLambdaExpression(BoundLambdaExpressionNode node) {
        checkStatement(node.body);
    }

    private void checkFunctionInvocation(BoundFunctionInvocationExpression node) {
        node.arguments.arguments.forEach(this::checkExpression);
    }

    private void addDiagnostic(ErrorCode code, Locatable locatable) {
        diagnostics.add(new DiagnosticMessage(code, locatable));
    }
}