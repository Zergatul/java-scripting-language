package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class BooleanLiteralExpressionNode extends ExpressionNode {

    public final boolean value;

    public BooleanLiteralExpressionNode(boolean value, TextRange range) {
        super(ParserNodeType.BOOLEAN_LITERAL, range);
        this.value = value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BooleanLiteralExpressionNode other) {
            return other.value == value && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}