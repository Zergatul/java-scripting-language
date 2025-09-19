package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundStaticReferenceExpression extends BoundExpressionNode {

    public final BoundTypeNode typeNode;

    public BoundStaticReferenceExpression(BoundTypeNode typeNode, SType type, TextRange range) {
        super(NodeType.STATIC_REFERENCE, type, range);
        this.typeNode = typeNode;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}