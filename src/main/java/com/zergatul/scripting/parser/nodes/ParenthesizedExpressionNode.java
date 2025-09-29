package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ParenthesizedExpressionNode extends ExpressionNode {

    public final Token openParen;
    public final ExpressionNode inner;
    public final Token closeParen;

    public ParenthesizedExpressionNode(Token openParen, ExpressionNode inner, Token closeParen) {
        super(ParserNodeType.PARENTHESIZED_EXPRESSION, TextRange.combine(openParen, closeParen));
        this.openParen = openParen;
        this.inner = inner;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        inner.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(openParen, inner, closeParen);
    }
}