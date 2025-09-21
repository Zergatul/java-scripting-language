package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundClassFieldNode extends BoundClassMemberNode {

    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;

    public BoundClassFieldNode(BoundTypeNode typeNode, BoundNameExpressionNode name, TextRange range) {
        super(BoundNodeType.CLASS_FIELD, range);
        this.typeNode = typeNode;
        this.name = name;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        name.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, name);
    }
}