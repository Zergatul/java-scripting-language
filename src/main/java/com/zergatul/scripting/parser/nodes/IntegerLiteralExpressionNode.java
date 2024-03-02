package com.zergatul.scripting.parser.nodes;

public class IntegerLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public IntegerLiteralExpressionNode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerLiteralExpressionNode other) {
            return other.value.equals(value);
        } else {
            return false;
        }
    }

    @Override
    public void print(String prefix) {
        System.out.print(prefix);
        System.out.println("+---IntegerLiteral: " + value);
    }
}