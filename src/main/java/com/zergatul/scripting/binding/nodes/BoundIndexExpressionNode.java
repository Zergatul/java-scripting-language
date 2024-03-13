package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.BinaryOperation;

public class BoundIndexExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final BoundExpressionNode index;
    public final BinaryOperation operation;

    public BoundIndexExpressionNode(BoundExpressionNode callee, BoundExpressionNode index, BinaryOperation operation, TextRange range) {
        super(NodeType.INDEX_EXPRESSION, operation.type, range);
        this.callee = callee;
        this.index = index;
        this.operation = operation;
    }
}