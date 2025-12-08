package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class NullExpressionNode extends ExpressionNode {

    public final Token token;

    public NullExpressionNode(Token token) {
        super(ParserNodeType.NULL_EXPRESSION, token.getRange());
        this.token = token;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {

    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of();
    }
}