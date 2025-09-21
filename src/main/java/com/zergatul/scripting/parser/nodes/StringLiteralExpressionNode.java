package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class StringLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public StringLiteralExpressionNode(ValueToken token) {
        this(token.value, token.getRange());
    }

    public StringLiteralExpressionNode(String value, TextRange range) {
        super(ParserNodeType.STRING_LITERAL, range);
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
        if (obj instanceof StringLiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}