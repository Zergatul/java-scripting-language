package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ParenthesizedExpressionNode extends ExpressionNode {

    public final ExpressionNode inner;

    public ParenthesizedExpressionNode(ExpressionNode inner, TextRange range) {
        super(ParserNodeType.PARENTHESIZED_EXPRESSION, range);
        this.inner = inner;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        inner.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParenthesizedExpressionNode other) {
            return other.inner.equals(inner) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}