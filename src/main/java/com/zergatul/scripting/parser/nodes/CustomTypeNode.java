package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class CustomTypeNode extends TypeNode {

    public final ValueToken token;
    public final String value;

    public CustomTypeNode(ValueToken token) {
        super(ParserNodeType.CUSTOM_TYPE, token.getRange());
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