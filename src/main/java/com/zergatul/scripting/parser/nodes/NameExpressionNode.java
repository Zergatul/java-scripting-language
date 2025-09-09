package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class NameExpressionNode extends ExpressionNode {

    public final ValueToken token;
    public final String value;

    public NameExpressionNode(ValueToken token) {
        super(ParserNodeType.NAME_EXPRESSION, token.getRange());
        this.token = token;
        this.value = token.value;
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