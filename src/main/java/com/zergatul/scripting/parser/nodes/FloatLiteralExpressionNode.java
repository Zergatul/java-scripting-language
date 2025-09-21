package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class FloatLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public FloatLiteralExpressionNode(ValueToken token) {
        this(token.value, token.getRange());
    }

    public FloatLiteralExpressionNode(String value, TextRange range) {
        super(ParserNodeType.FLOAT_LITERAL, range);
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
        if (obj instanceof FloatLiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}