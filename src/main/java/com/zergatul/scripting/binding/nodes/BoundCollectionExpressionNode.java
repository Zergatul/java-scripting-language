package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.CollectionExpressionNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundCollectionExpressionNode extends BoundExpressionNode {

    public final Token openBracket;
    public final BoundSeparatedList<BoundExpressionNode> list;
    public final Token closeBracket;

    public BoundCollectionExpressionNode(BoundEmptyCollectionExpressionNode node, SType type) {
        this(type, node.openBracket, BoundSeparatedList.of(), node.closeBracket, node.getRange());
    }

    public BoundCollectionExpressionNode(CollectionExpressionNode node, SType type, BoundSeparatedList<BoundExpressionNode> list) {
        this(type, node.openBracket, list, node.closeBracket, node.getRange());
    }

    public BoundCollectionExpressionNode(SType type, Token openBracket, BoundSeparatedList<BoundExpressionNode> list, Token closeBracket, TextRange range) {
        super(BoundNodeType.COLLECTION_EXPRESSION, type, range);
        this.openBracket = openBracket;
        this.list = list;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundExpressionNode item : list.getNodes()) {
            item.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(list.getNodes());
    }
}