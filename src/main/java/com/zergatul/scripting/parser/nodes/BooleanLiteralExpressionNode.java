package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class BooleanLiteralExpressionNode extends ExpressionNode {

    public final Token token;
    public final boolean value;

    public BooleanLiteralExpressionNode(Token token, boolean value) {
        super(ParserNodeType.BOOLEAN_LITERAL, token.getRange());
        this.token = token;
        this.value = value;
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