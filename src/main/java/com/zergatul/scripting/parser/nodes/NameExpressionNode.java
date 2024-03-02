package com.zergatul.scripting.parser.nodes;

public class NameExpressionNode extends ExpressionNode {

    public final String value;

    public NameExpressionNode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NameExpressionNode other) {
            return other.value.equals(value);
        } else {
            return false;
        }
    }
}