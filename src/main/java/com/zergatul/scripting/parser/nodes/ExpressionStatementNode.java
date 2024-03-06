package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class ExpressionStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression, TextRange range) {
        super(range);
        this.expression = expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpressionStatementNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}