package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class AwaitExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;

    public AwaitExpressionNode(ExpressionNode expression, TextRange range) {
        super(NodeType.AWAIT_EXPRESSION, range);
        this.expression = expression;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AwaitExpressionNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}