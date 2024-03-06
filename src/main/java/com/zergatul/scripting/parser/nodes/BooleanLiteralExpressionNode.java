package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class BooleanLiteralExpressionNode extends LiteralExpressionNode {

    public final boolean value;

    public BooleanLiteralExpressionNode(boolean value, TextRange range) {
        super(range);
        this.value = value;
    }
}