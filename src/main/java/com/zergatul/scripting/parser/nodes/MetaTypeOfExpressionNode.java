package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class MetaTypeOfExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final Token openParen;
    public final ExpressionNode expression;
    public final Token closeParen;

    public MetaTypeOfExpressionNode(Token keyword, Token openParen, ExpressionNode expression, Token closeParen) {
        super(ParserNodeType.META_TYPE_OF_EXPRESSION, TextRange.combine(keyword, closeParen));
        this.keyword = keyword;
        this.openParen = openParen;
        this.expression = expression;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, openParen, expression, closeParen);
    }
}