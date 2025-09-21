package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundPredefinedTypeNode extends BoundTypeNode {

    public BoundPredefinedTypeNode(SType type, TextRange range) {
        super(BoundNodeType.PREDEFINED_TYPE, type, range);
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundPredefinedTypeNode other) {
            return other.type.equals(type) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}