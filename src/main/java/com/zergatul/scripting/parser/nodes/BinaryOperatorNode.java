package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class BinaryOperatorNode extends ParserNode {

    public final Token token;
    public final BinaryOperator operator;

    public BinaryOperatorNode(Token token, BinaryOperator operator) {
        super(ParserNodeType.BINARY_OPERATOR, token.getRange());
        this.token = token;
        this.operator = operator;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}