package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.BinaryExpressionNode;

import java.util.List;

public class BoundBinaryExpressionNode extends BoundExpressionNode {

    public final BinaryExpressionNode syntaxNode;
    public final BoundExpressionNode left;
    public final BoundBinaryOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundBinaryExpressionNode(BoundExpressionNode left, BoundBinaryOperatorNode operator, BoundExpressionNode right) {
        this(SyntaxFactory.missingBinaryExpression(), left, operator, right, TextRange.MISSING);
    }

    public BoundBinaryExpressionNode(BinaryExpressionNode node, BoundExpressionNode left, BoundBinaryOperatorNode operator, BoundExpressionNode right) {
        this(node, left, operator, right, node.getRange());
    }

    public BoundBinaryExpressionNode(BinaryExpressionNode node, BoundExpressionNode left, BoundBinaryOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(BoundNodeType.BINARY_EXPRESSION, operator.operation.type, range);
        this.syntaxNode = node;
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        left.accept(visitor);
        operator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(left, operator, right);
    }
}