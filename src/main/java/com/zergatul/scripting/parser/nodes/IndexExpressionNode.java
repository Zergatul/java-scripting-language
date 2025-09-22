package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class IndexExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final Token openBracket;
    public final ExpressionNode index;
    public final Token closeBracket;

    public IndexExpressionNode(ExpressionNode callee, Token openBracket, ExpressionNode index, Token closeBracket) {
        super(ParserNodeType.INDEX_EXPRESSION, TextRange.combine(callee, closeBracket));
        this.callee = callee;
        this.openBracket = openBracket;
        this.index = index;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        callee.accept(visitor);
        index.accept(visitor);
    }
}