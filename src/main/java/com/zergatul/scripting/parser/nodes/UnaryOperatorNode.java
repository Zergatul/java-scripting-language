package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.UnaryOperator;

import java.util.List;

public class UnaryOperatorNode extends ParserNode {

    public final Token token;
    public final UnaryOperator operator;

    public UnaryOperatorNode(Token token, UnaryOperator operator) {
        super(ParserNodeType.UNARY_OPERATOR, token.getRange());
        this.token = token;
        this.operator = operator;
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