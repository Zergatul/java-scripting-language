package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NullExpressionNode;
import com.zergatul.scripting.type.SNull;

import java.util.List;

public class BoundNullExpressionNode extends BoundExpressionNode {

    public final NullExpressionNode syntaxNode;

    public BoundNullExpressionNode(NullExpressionNode node) {
        super(BoundNodeType.NULL_EXPRESSION, SNull.instance, node.getRange());
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