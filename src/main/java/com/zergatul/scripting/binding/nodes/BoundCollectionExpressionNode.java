package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CollectionExpressionNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundCollectionExpressionNode extends BoundExpressionNode {

    public final CollectionExpressionNode syntaxNode;
    public final List<BoundExpressionNode> list;

    public BoundCollectionExpressionNode(BoundEmptyCollectionExpressionNode node, SType type) {
        this(node.syntaxNode, type, List.of(), node.getRange());
    }

    public BoundCollectionExpressionNode(CollectionExpressionNode node, SType type, List<BoundExpressionNode> list) {
        this(node, type, list, node.getRange());
    }

    public BoundCollectionExpressionNode(CollectionExpressionNode node, SType type, List<BoundExpressionNode> list, TextRange range) {
        super(BoundNodeType.COLLECTION_EXPRESSION, type, range);
        this.syntaxNode = node;
        this.list = list;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundExpressionNode item : list) {
            item.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(list);
    }
}