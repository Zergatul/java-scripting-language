package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class MetaCastExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final Token openParen;
    public final ExpressionNode expression;
    public final Token comma;
    public final TypeNode type;
    public final Token closeParen;

    public MetaCastExpressionNode(Token keyword, Token openParen, ExpressionNode expression, Token comma, TypeNode type, Token closeParen) {
        super(ParserNodeType.META_CAST_EXPRESSION, TextRange.combine(keyword, closeParen));
        this.keyword = keyword;
        this.openParen = openParen;
        this.expression = expression;
        this.comma = comma;
        this.type = type;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, openParen, expression, comma, type, closeParen);
    }
}