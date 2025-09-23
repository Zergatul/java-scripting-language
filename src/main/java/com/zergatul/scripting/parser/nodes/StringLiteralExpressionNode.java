package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class StringLiteralExpressionNode extends ExpressionNode {

    public final ValueToken token;
    public final String value;

    public StringLiteralExpressionNode(ValueToken token) {
        super(ParserNodeType.STRING_LITERAL, token.getRange());
        this.token = token;
        this.value = token.value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}