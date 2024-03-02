package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.parser.UnaryOperator;

public class UnaryExpressionNode extends ExpressionNode {

    public final UnaryOperator operator;
    public final ExpressionNode operand;

    public UnaryExpressionNode(UnaryOperator operator, ExpressionNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public void print(String prefix) {
        System.out.print(prefix);
        System.out.println("+---UnaryExpression");

        prefix += "|   ";

        System.out.print(prefix);
        System.out.println("+---" + operator);
        operand.print(prefix);
    }
}