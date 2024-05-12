package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
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
    public List<BoundNode> getChildren() {
        return List.of(left, operator, right);
    }
}