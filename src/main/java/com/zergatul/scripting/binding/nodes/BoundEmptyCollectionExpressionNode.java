package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CollectionExpressionNode;
import com.zergatul.scripting.type.SEmptyCollection;

import java.util.List;

public class BoundEmptyCollectionExpressionNode extends BoundExpressionNode {

    public final CollectionExpressionNode syntaxNode;

    public BoundEmptyCollectionExpressionNode(CollectionExpressionNode node) {
        this(node, node.getRange());
    }

    public BoundEmptyCollectionExpressionNode(CollectionExpressionNode node, TextRange range) {
        super(BoundNodeType.EMPTY_COLLECTION_EXPRESSION, SEmptyCollection.instance, range);
        this.syntaxNode = node;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}