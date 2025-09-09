package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class IntegerLiteralExpressionNode extends ExpressionNode {

    @Nullable
    public Token sign;
    public ValueToken token;
    public final String value;

    public IntegerLiteralExpressionNode(
            @Nullable Token sign,
            ValueToken token
    ) {
        super(ParserNodeType.INTEGER_LITERAL, sign == null ? token.getRange() : TextRange.combine(sign, token));
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

    @Override
    public List<Locatable> getChildNodes() {
        if (sign != null) {
            return List.of(sign, token);
        } else {
            return List.of(token);
        }
    }

    public IntegerLiteralExpressionNode withSign(UnaryOperatorNode operator) {
        return new IntegerLiteralExpressionNode(operator.token, token);
    }
}