package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.Objects;

public class ReturnStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public ReturnStatementNode(ExpressionNode expression, TextRange range) {
        super(NodeType.RETURN_STATEMENT, range);
        this.expression = expression;
    }

    @Override
    public boolean isAsync() {
        return expression != null && expression.isAsync();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReturnStatementNode other) {
            return Objects.equals(other.expression, expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}