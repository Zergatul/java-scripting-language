package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class FloatLiteralExpressionNode extends ExpressionNode {

    public final ValueToken token;
    public final String value;

    public FloatLiteralExpressionNode(ValueToken token) {
        super(ParserNodeType.FLOAT_LITERAL, token.getRange());
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