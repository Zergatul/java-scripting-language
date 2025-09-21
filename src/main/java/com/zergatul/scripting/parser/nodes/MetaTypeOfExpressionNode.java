package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class MetaTypeOfExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final Token openParen;
    public final ExpressionNode expression;
    public final Token closeParen;

    public MetaTypeOfExpressionNode(Token keyword, Token openParen, ExpressionNode expression, Token closeParen, TextRange range) {
        super(ParserNodeType.META_TYPE_OF_EXPRESSION, range);
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
    public boolean equals(Object obj) {
        if (obj instanceof MetaTypeOfExpressionNode other) {
            return  other.keyword.equals(keyword) &&
                    other.openParen.equals(openParen) &&
                    other.expression.equals(expression) &&
                    other.closeParen.equals(closeParen) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}