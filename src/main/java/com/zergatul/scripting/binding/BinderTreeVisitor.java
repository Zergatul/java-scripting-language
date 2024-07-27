package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.*;

public abstract class BinderTreeVisitor {

    public void explicitVisit(BoundArgumentsListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundArrayTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundAssignmentOperatorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundAssignmentStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundAugmentedAssignmentStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundAwaitExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundBinaryExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundBinaryOperatorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundBlockStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundBooleanLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundBreakStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundCharLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundCompilationUnitNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundConditionalExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundContinueStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundEmptyStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundExpressionStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundFloatLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundForEachLoopStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundForLoopStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundFunctionInvocationExpression node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundFunctionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundFunctionsListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundIfStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundImplicitCastExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundIndexExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundIntegerLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundInvalidExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundInvalidStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundInvalidTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundLambdaExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundMethodInvocationExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundMethodNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundNameExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundNewExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundParameterListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundParameterNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundPostfixStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundPredefinedTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundPropertyAccessExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundPropertyNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundRefArgumentExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundRefTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundReturnStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundStatementsListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundStaticReferenceExpression node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundStaticVariablesListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundStringLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundUnaryExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundUnaryOperatorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundVariableDeclarationNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundVoidTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundWhileLoopStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void visit(BoundArgumentsListNode node) {}
    public void visit(BoundArrayTypeNode node) {}
    public void visit(BoundAssignmentOperatorNode node) {}
    public void visit(BoundAssignmentStatementNode node) {}
    public void visit(BoundAugmentedAssignmentStatementNode node) {}
    public void visit(BoundAwaitExpressionNode node) {}
    public void visit(BoundBinaryExpressionNode node) {}
    public void visit(BoundBinaryOperatorNode node) {}
    public void visit(BoundBlockStatementNode node) {}
    public void visit(BoundBooleanLiteralExpressionNode node) {}
    public void visit(BoundBreakStatementNode node) {}
    public void visit(BoundCharLiteralExpressionNode node) {}
    public void visit(BoundCompilationUnitNode node) {}
    public void visit(BoundConditionalExpressionNode node) {}
    public void visit(BoundContinueStatementNode node) {}
    public void visit(BoundEmptyStatementNode node) {}
    public void visit(BoundExpressionStatementNode node) {}
    public void visit(BoundFloatLiteralExpressionNode node) {}
    public void visit(BoundForEachLoopStatementNode node) {}
    public void visit(BoundForLoopStatementNode node) {}
    public void visit(BoundFunctionInvocationExpression node) {}
    public void visit(BoundFunctionNode node) {}
    public void visit(BoundFunctionsListNode node) {}
    public void visit(BoundIfStatementNode node) {}
    public void visit(BoundImplicitCastExpressionNode node) {}
    public void visit(BoundIndexExpressionNode node) {}
    public void visit(BoundIntegerLiteralExpressionNode node) {}
    public void visit(BoundInvalidExpressionNode node) {}
    public void visit(BoundInvalidStatementNode node) {}
    public void visit(BoundInvalidTypeNode node) {}
    public void visit(BoundLambdaExpressionNode node) {}
    public void visit(BoundMethodInvocationExpressionNode node) {}
    public void visit(BoundMethodNode node) {}
    public void visit(BoundNameExpressionNode node) {}
    public void visit(BoundNewExpressionNode node) {}
    public void visit(BoundParameterListNode node) {}
    public void visit(BoundParameterNode node) {}
    public void visit(BoundPostfixStatementNode node) {}
    public void visit(BoundPredefinedTypeNode node) {}
    public void visit(BoundPropertyAccessExpressionNode node) {}
    public void visit(BoundPropertyNode node) {}
    public void visit(BoundRefArgumentExpressionNode node) {}
    public void visit(BoundRefTypeNode node) {}
    public void visit(BoundReturnStatementNode node) {}
    public void visit(BoundStatementsListNode node) {}
    public void visit(BoundStaticReferenceExpression node) {}
    public void visit(BoundStaticVariablesListNode node) {}
    public void visit(BoundStringLiteralExpressionNode node) {}
    public void visit(BoundUnaryExpressionNode node) {}
    public void visit(BoundUnaryOperatorNode node) {}
    public void visit(BoundVariableDeclarationNode node) {}
    public void visit(BoundVoidTypeNode node) {}
    public void visit(BoundWhileLoopStatementNode node) {}
}