package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class BaseExpressionNode extends ExpressionNode {

    public final Token token;

    public BaseExpressionNode(Token token) {
        super(ParserNodeType.BASE_EXPRESSION, token.getRange());
        this.token = token;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(token);
    }
}