package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.UnaryOperation;

public class BoundImplicitCastExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode operand;
    public final UnaryOperation operation;

    public BoundImplicitCastExpressionNode(BoundExpressionNode operand, UnaryOperation operation, TextRange range) {
        super(NodeType.IMPLICIT_CAST, operand.type, range);
        this.operand = operand;
        this.operation = operation;
    }
}