package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundBinaryExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode left;
    public final BoundBinaryOperatorNode operator;
    public final BoundExpressionNode right;

    public BoundBinaryExpressionNode(BoundExpressionNode left, BoundBinaryOperatorNode operator, BoundExpressionNode right, TextRange range) {
        super(NodeType.BINARY_EXPRESSION, operator.operation.type, range);
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