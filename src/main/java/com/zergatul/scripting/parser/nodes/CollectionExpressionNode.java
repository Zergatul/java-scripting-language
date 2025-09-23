package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class CollectionExpressionNode extends ExpressionNode {

    public final Token openBracket;
    public final SeparatedList<ExpressionNode> list;
    public final Token closeBracket;

    public CollectionExpressionNode(Token openBracket, SeparatedList<ExpressionNode> list, Token closeBracket, TextRange range) {
        super(ParserNodeType.COLLECTION_EXPRESSION, range);
        this.openBracket = openBracket;
        this.list = list;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ExpressionNode item : list.getNodes()) {
            item.accept(visitor);
        }
    }
}