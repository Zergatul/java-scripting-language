package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class Integer64LiteralExpressionNode extends ExpressionNode {

    public Token sign;
    public ValueToken token;
    public final String value;

    public Integer64LiteralExpressionNode(Token sign, ValueToken token) {
        super(ParserNodeType.INTEGER64_LITERAL, sign == null ? token.getRange() : TextRange.combine(sign, token));
        this.sign = sign;
        this.token = token;
        if (sign != null) {
            this.value = sign.is(TokenType.MINUS) ? "-" + token.value : token.value;
        } else {
            this.value = token.value;
        }
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    public Integer64LiteralExpressionNode withSign(UnaryOperatorNode operator) {
        return new Integer64LiteralExpressionNode(operator.token, token);
    }
}