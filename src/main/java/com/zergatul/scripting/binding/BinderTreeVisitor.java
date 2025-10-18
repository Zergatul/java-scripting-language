package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.*;

public abstract class BinderTreeVisitor {

    public void explicitVisit(BoundArgumentsListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundArrayCreationExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundArrayInitializerExpressionNode node) {
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

    public void explicitVisit(BoundClassConstructorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundClassFieldNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundClassMethodNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundClassNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundCollectionExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundCompilationUnitNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundCompilationUnitMembersListNode node) {
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

    public void explicitVisit(BoundConversionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundCustomTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundEmptyCollectionExpressionNode node) {
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

    public void explicitVisit(BoundFunctionAsLambdaExpressionNode node) {
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

    public void explicitVisit(BoundFunctionReferenceNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundJavaTypeNode node) {
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

    public void explicitVisit(BoundInExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundIntegerLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundInteger64LiteralExpressionNode node) {
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

    public void explicitVisit(BoundLetTypeNode node) {
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

    public void explicitVisit(BoundObjectCreationExpressionNode node) {
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

    public void explicitVisit(BoundParenthesizedExpressionNode node) {
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

    public void explicitVisit(BoundStaticVariableNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundStaticReferenceExpression node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundStringLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundThisExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundTypeCastExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BoundTypeTestExpressionNode node) {
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

    public void explicitVisit(BoundUnconvertedLambdaExpressionNode node) {
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
    public void visit(BoundArrayCreationExpressionNode node) {}
    public void visit(BoundArrayInitializerExpressionNode node) {}
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
    public void visit(BoundClassNode node) {}
    public void visit(BoundCharLiteralExpressionNode node) {}
    public void visit(BoundCollectionExpressionNode node) {}
    public void visit(BoundCompilationUnitNode node) {}
    public void visit(BoundCompilationUnitMembersListNode node) {}
    public void visit(BoundConditionalExpressionNode node) {}
    public void visit(BoundContinueStatementNode node) {}
    public void visit(BoundConversionNode node) {}
    public void visit(BoundCustomTypeNode node) {}
    public void visit(BoundEmptyCollectionExpressionNode node) {}
    public void visit(BoundEmptyStatementNode node) {}
    public void visit(BoundClassConstructorNode node) {}
    public void visit(BoundClassFieldNode node) {}
    public void visit(BoundClassMethodNode node) {}
    public void visit(BoundExpressionStatementNode node) {}
    public void visit(BoundFloatLiteralExpressionNode node) {}
    public void visit(BoundForEachLoopStatementNode node) {}
    public void visit(BoundForLoopStatementNode node) {}
    public void visit(BoundFunctionAsLambdaExpressionNode node) {}
    public void visit(BoundFunctionInvocationExpression node) {}
    public void visit(BoundFunctionNode node) {}
    public void visit(BoundFunctionReferenceNode node) {}
    public void visit(BoundJavaTypeNode node) {}
    public void visit(BoundIfStatementNode node) {}
    public void visit(BoundImplicitCastExpressionNode node) {}
    public void visit(BoundIndexExpressionNode node) {}
    public void visit(BoundInExpressionNode node) {}
    public void visit(BoundIntegerLiteralExpressionNode node) {}
    public void visit(BoundInteger64LiteralExpressionNode node) {}
    public void visit(BoundInvalidExpressionNode node) {}
    public void visit(BoundInvalidStatementNode node) {}
    public void visit(BoundInvalidTypeNode node) {}
    public void visit(BoundLambdaExpressionNode node) {}
    public void visit(BoundLetTypeNode node) {}
    public void visit(BoundMethodInvocationExpressionNode node) {}
    public void visit(BoundMethodNode node) {}
    public void visit(BoundNameExpressionNode node) {}
    public void visit(BoundObjectCreationExpressionNode node) {}
    public void visit(BoundParameterListNode node) {}
    public void visit(BoundParameterNode node) {}
    public void visit(BoundParenthesizedExpressionNode node) {}
    public void visit(BoundPostfixStatementNode node) {}
    public void visit(BoundPredefinedTypeNode node) {}
    public void visit(BoundPropertyAccessExpressionNode node) {}
    public void visit(BoundPropertyNode node) {}
    public void visit(BoundRefArgumentExpressionNode node) {}
    public void visit(BoundRefTypeNode node) {}
    public void visit(BoundReturnStatementNode node) {}
    public void visit(BoundStatementsListNode node) {}
    public void visit(BoundStaticVariableNode node) {}
    public void visit(BoundStaticReferenceExpression node) {}
    public void visit(BoundStringLiteralExpressionNode node) {}
    public void visit(BoundThisExpressionNode node) {}
    public void visit(BoundTypeCastExpressionNode node) {}
    public void visit(BoundTypeTestExpressionNode node) {}
    public void visit(BoundUnaryExpressionNode node) {}
    public void visit(BoundUnaryOperatorNode node) {}
    public void visit(BoundUnconvertedLambdaExpressionNode node) {}
    public void visit(BoundVariableDeclarationNode node) {}
    public void visit(BoundVoidTypeNode node) {}
    public void visit(BoundWhileLoopStatementNode node) {}
}