package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;

public class BoundIndexExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final BoundExpressionNode index;
    public final IndexOperation operation;

    public BoundIndexExpressionNode(BoundExpressionNode callee, BoundExpressionNode index, IndexOperation operation, TextRange range) {
        super(NodeType.INDEX_EXPRESSION, operation.type, range);
        this.callee = callee;
        this.index = index;
        this.operation = operation;
    }

    @Override
    public boolean canSet() {
        return operation.canSet();
    }
}