package com.zergatul.scripting.parser.nodes;

public class ExpressionStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpressionStatementNode other) {
            return other.expression.equals(expression);
        } else {
            return false;
        }
    }
}