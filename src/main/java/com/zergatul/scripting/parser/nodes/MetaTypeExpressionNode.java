package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class MetaTypeExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final Token openParen;
    public final TypeNode type;
    public final Token closeParen;

    public MetaTypeExpressionNode(Token keyword, Token openParen, TypeNode type, Token closeParen) {
        super(ParserNodeType.META_TYPE_EXPRESSION, TextRange.combine(keyword, closeParen));
        this.keyword = keyword;
        this.openParen = openParen;
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
}