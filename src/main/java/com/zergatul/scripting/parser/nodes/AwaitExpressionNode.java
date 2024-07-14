package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class AwaitExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;

    public AwaitExpressionNode(ExpressionNode expression, TextRange range) {
        super(NodeType.AWAIT_EXPRESSION, range);
        this.expression = expression;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        expression.accept(visitor);
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