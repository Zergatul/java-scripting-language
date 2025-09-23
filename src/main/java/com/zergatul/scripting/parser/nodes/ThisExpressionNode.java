package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ThisExpressionNode extends ExpressionNode {

    public final Token token;

    public ThisExpressionNode(Token token) {
        super(ParserNodeType.THIS_EXPRESSION, token.getRange());
        this.token = token;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}