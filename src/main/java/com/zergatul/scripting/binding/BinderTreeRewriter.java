package com.zergatul.scripting.binding;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BinderTreeRewriter {

    public <T extends BoundNode> T rewrite(T node) {
        if (node == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T result = (T) node.accept(this);
        return result;
    }

    protected <T extends BoundNode> List<T> rewriteList(List<T> list) {
        boolean anyChanged = false;
        List<T> tmp = new ArrayList<>(list.size());
        for (T item : list) {
            T rewritten = rewrite(item);
            anyChanged |= (rewritten != item);
            tmp.add(rewritten);
        }
        return anyChanged ? Collections.unmodifiableList(tmp) : list;
    }

    public BoundArgumentsListNode visit(BoundArgumentsListNode node) {
        return node.update(rewriteList(node.arguments));
    }

    public BoundArrayCreationExpressionNode visit(BoundArrayCreationExpressionNode node) { throw new InternalException(); }
    public BoundArrayInitializerExpressionNode visit(BoundArrayInitializerExpressionNode node) { throw new InternalException(); }
    public BoundArrayTypeNode visit(BoundArrayTypeNode node) { throw new InternalException(); }
    public BoundAssignmentOperatorNode visit(BoundAssignmentOperatorNode node) { throw new InternalException(); }

    public BoundAssignmentStatementNode visit(BoundAssignmentStatementNode node) {
        return node.update(rewrite(node.left), rewrite(node.right));
    }

    public BoundAugmentedAssignmentStatementNode visit(BoundAugmentedAssignmentStatementNode node) { throw new InternalException(); }

    public BoundAwaitExpressionNode visit(BoundAwaitExpressionNode node) {
        return node.update(rewrite(node.expression));
    }

    public BoundBinaryExpressionNode visit(BoundBinaryExpressionNode node) {
        return node.update(rewrite(node.left), rewrite(node.right));
    }

    public BoundBinaryOperatorNode visit(BoundBinaryOperatorNode node) { throw new InternalException(); }
    public BoundBlockStatementNode visit(BoundBlockStatementNode node) { throw new InternalException(); }
    public BoundBooleanLiteralExpressionNode visit(BoundBooleanLiteralExpressionNode node) { throw new InternalException(); }
    public BoundBreakStatementNode visit(BoundBreakStatementNode node) { throw new InternalException(); }
    public BoundClassNode visit(BoundClassNode node) { throw new InternalException(); }
    public BoundCharLiteralExpressionNode visit(BoundCharLiteralExpressionNode node) { throw new InternalException(); }
    public BoundCollectionExpressionNode visit(BoundCollectionExpressionNode node) { throw new InternalException(); }
    public BoundCompilationUnitNode visit(BoundCompilationUnitNode node) { throw new InternalException(); }
    public BoundCompilationUnitMembersListNode visit(BoundCompilationUnitMembersListNode node) { throw new InternalException(); }
    public BoundConditionalExpressionNode visit(BoundConditionalExpressionNode node) { throw new InternalException(); }
    public BoundContinueStatementNode visit(BoundContinueStatementNode node) { throw new InternalException(); }
    public BoundCustomTypeNode visit(BoundCustomTypeNode node) { throw new InternalException(); }
    public BoundEmptyStatementNode visit(BoundEmptyStatementNode node) { throw new InternalException(); }
    public BoundClassConstructorNode visit(BoundClassConstructorNode node) { throw new InternalException(); }
    public BoundClassFieldNode visit(BoundClassFieldNode node) { throw new InternalException(); }
    public BoundClassMethodNode visit(BoundClassMethodNode node) { throw new InternalException(); }

    public BoundExpressionStatementNode visit(BoundExpressionStatementNode node) {
        return node.update(rewrite(node.expression));
    }

    public BoundFloatLiteralExpressionNode visit(BoundFloatLiteralExpressionNode node) { throw new InternalException(); }
    public BoundForEachLoopStatementNode visit(BoundForEachLoopStatementNode node) { throw new InternalException(); }
    public BoundForLoopStatementNode visit(BoundForLoopStatementNode node) { throw new InternalException(); }
    public BoundFunctionAsLambdaExpressionNode visit(BoundFunctionAsLambdaExpressionNode node) { throw new InternalException(); }
    public BoundFunctionInvocationExpression visit(BoundFunctionInvocationExpression node) { throw new InternalException(); }
    public BoundFunctionNode visit(BoundFunctionNode node) { throw new InternalException(); }
    public BoundJavaTypeNode visit(BoundJavaTypeNode node) { throw new InternalException(); }
    public BoundIfStatementNode visit(BoundIfStatementNode node) { throw new InternalException(); }
    public BoundImplicitCastExpressionNode visit(BoundImplicitCastExpressionNode node) { throw new InternalException(); }
    public BoundIndexExpressionNode visit(BoundIndexExpressionNode node) { throw new InternalException(); }
    public BoundIntegerLiteralExpressionNode visit(BoundIntegerLiteralExpressionNode node) { throw new InternalException(); }
    public BoundInteger64LiteralExpressionNode visit(BoundInteger64LiteralExpressionNode node) { throw new InternalException(); }
    public BoundInvalidExpressionNode visit(BoundInvalidExpressionNode node) { throw new InternalException(); }
    public BoundInvalidStatementNode visit(BoundInvalidStatementNode node) { throw new InternalException(); }
    public BoundInvalidTypeNode visit(BoundInvalidTypeNode node) { throw new InternalException(); }
    public BoundLambdaExpressionNode visit(BoundLambdaExpressionNode node) { throw new InternalException(); }
    public BoundLetTypeNode visit(BoundLetTypeNode node) { throw new InternalException(); }

    public BoundMethodInvocationExpressionNode visit(BoundMethodInvocationExpressionNode node) {
        return node.update(rewrite(node.objectReference), rewrite(node.arguments));
    }

    public BoundMethodNode visit(BoundMethodNode node) { throw new InternalException(); }

    public BoundNameExpressionNode visit(BoundNameExpressionNode node) {
        return node;
    }

    public BoundObjectCreationExpressionNode visit(BoundObjectCreationExpressionNode node) { throw new InternalException(); }
    public BoundParameterListNode visit(BoundParameterListNode node) { throw new InternalException(); }
    public BoundParameterNode visit(BoundParameterNode node) { throw new InternalException(); }
    public BoundPostfixStatementNode visit(BoundPostfixStatementNode node) { throw new InternalException(); }
    public BoundPredefinedTypeNode visit(BoundPredefinedTypeNode node) { throw new InternalException(); }

    public BoundPropertyAccessExpressionNode visit(BoundPropertyAccessExpressionNode node) {
        return node.update(rewrite(node.callee));
    }

    public BoundPropertyNode visit(BoundPropertyNode node) { throw new InternalException(); }
    public BoundRefArgumentExpressionNode visit(BoundRefArgumentExpressionNode node) { throw new InternalException(); }
    public BoundRefTypeNode visit(BoundRefTypeNode node) { throw new InternalException(); }

    public BoundReturnStatementNode visit(BoundReturnStatementNode node) {
        if (node.expression != null) {
            return node.update(rewrite(node.expression));
        } else {
            return node;
        }
    }

    public BoundStatementsListNode visit(BoundStatementsListNode node) { throw new InternalException(); }
    public BoundStaticVariableNode visit(BoundStaticVariableNode node) { throw new InternalException(); }
    public BoundStaticReferenceExpression visit(BoundStaticReferenceExpression node) { throw new InternalException(); }
    public BoundStringLiteralExpressionNode visit(BoundStringLiteralExpressionNode node) { throw new InternalException(); }

    public BoundExpressionNode visit(BoundThisExpressionNode node) {
        return node;
    }

    public BoundTypeCastExpressionNode visit(BoundTypeCastExpressionNode node) { throw new InternalException(); }
    public BoundTypeTestExpressionNode visit(BoundTypeTestExpressionNode node) { throw new InternalException(); }
    public BoundUnaryExpressionNode visit(BoundUnaryExpressionNode node) { throw new InternalException(); }
    public BoundUnaryOperatorNode visit(BoundUnaryOperatorNode node) { throw new InternalException(); }

    public BoundVariableDeclarationNode visit(BoundVariableDeclarationNode node) {
        if (node.expression != null) {
            return node.update(rewrite(node.expression));
        } else {
            throw new InternalException();
        }
    }

    public BoundVoidTypeNode visit(BoundVoidTypeNode node) { throw new InternalException(); }
    public BoundWhileLoopStatementNode visit(BoundWhileLoopStatementNode node) { throw new InternalException(); }
}