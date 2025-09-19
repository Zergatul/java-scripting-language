package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundCollectionExpressionNode extends BoundExpressionNode {

    public final List<BoundExpressionNode> items;

    public BoundCollectionExpressionNode(SType type, List<BoundExpressionNode> items, TextRange range) {
        super(NodeType.COLLECTION_EXPRESSION, type, range);
        this.items = items;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundExpressionNode item : items) {
            item.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(items);
    }
}
