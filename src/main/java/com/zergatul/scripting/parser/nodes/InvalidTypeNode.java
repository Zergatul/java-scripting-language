package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidTypeNode extends TypeNode {

    public final Token token;

    public InvalidTypeNode(Token token) {
        super(ParserNodeType.INVALID_TYPE, token.getRange());
        this.token = token;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}