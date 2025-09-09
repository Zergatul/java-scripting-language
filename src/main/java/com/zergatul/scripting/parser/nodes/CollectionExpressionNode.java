package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(openBracket);
        nodes.addAll(list.getChildNodes());
        nodes.add(closeBracket);
        return nodes;
    }
}