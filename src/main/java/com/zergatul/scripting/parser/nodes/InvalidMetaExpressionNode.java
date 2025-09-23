package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidMetaExpressionNode extends ExpressionNode {

    public final Token token;

    public InvalidMetaExpressionNode(Token token) {
        super(ParserNodeType.META_INVALID_EXPRESSION, token.getRange());
        this.token = token;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}