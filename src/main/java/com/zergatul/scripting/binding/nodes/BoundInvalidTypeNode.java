package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidTypeNode extends BoundTypeNode {

    public BoundInvalidTypeNode(TextRange range) {
        super(BoundNodeType.INVALID_TYPE, SUnknown.instance, range);
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
