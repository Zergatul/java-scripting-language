package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class CollectionExpressionNode extends ExpressionNode {

    public final Token openBracket;
    public final List<ExpressionNode> items;
    public final Token closeBracket;

    public CollectionExpressionNode(Token openBracket, List<ExpressionNode> items, Token closeBracket, TextRange range) {
        super(ParserNodeType.COLLECTION_EXPRESSION, range);
        this.openBracket = openBracket;
        this.items = items;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ExpressionNode item : items) {
            item.accept(visitor);
        }
    }
}