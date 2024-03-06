package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class InvalidExpressionNode extends ExpressionNode {
    public InvalidExpressionNode(TextRange range) {
        super(range);
    }
}