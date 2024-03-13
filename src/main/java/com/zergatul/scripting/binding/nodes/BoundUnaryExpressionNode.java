package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundUnaryExpressionNode extends BoundExpressionNode {

    public final BoundUnaryOperatorNode operator;
    public final BoundExpressionNode operand;

    public BoundUnaryExpressionNode(BoundUnaryOperatorNode operator, BoundExpressionNode operand, TextRange range) {
        super(NodeType.UNARY_EXPRESSION, operator.operation.type, range);
        this.operator = operator;
        this.operand = operand;
    }
}