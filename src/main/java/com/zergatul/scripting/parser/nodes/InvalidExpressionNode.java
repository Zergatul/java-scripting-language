package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class InvalidExpressionNode extends ExpressionNode {
    public InvalidExpressionNode(TextRange range) {
        super(NodeType.INVALID_EXPRESSION, range);
    }
}