package com.zergatul.scripting.parser.nodes;

public class BooleanLiteralExpressionNode extends LiteralExpressionNode {

    public final boolean value;

    public BooleanLiteralExpressionNode(boolean value) {
        this.value = value;
    }
}