package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.parser.BinaryOperator;

public class BinaryExpressionNode extends ExpressionNode {

    public final ExpressionNode left;
    public final BinaryOperator operator;
    public final ExpressionNode right;

    public BinaryExpressionNode(ExpressionNode left, BinaryOperator operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryExpressionNode other) {
            return other.left.equals(left) && other.operator.equals(operator) && other.right.equals(right);
        } else {
            return false;
        }
    }

    @Override
    public void print(String prefix) {
        System.out.print(prefix);
        System.out.println("+---BinaryExpression");

        prefix += "|    ";

        left.print(prefix);
        System.out.print(prefix);
        System.out.println("+---" + operator);
        right.print(prefix);
    }
}