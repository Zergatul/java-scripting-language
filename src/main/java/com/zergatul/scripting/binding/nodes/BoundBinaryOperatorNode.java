package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.BinaryOperation;

public class BoundBinaryOperatorNode extends BoundNode {

    public final BinaryOperation operation;

    public BoundBinaryOperatorNode(BinaryOperation operation, TextRange range) {
        super(NodeType.BINARY_OPERATOR, range);
        this.operation = operation;
    }
}