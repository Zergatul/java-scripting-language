package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundStackLoadNode extends BoundExpressionNode {

    public final int index;

    public BoundStackLoadNode(int index, SType type) {
        super(BoundNodeType.STACK_LOAD, type, TextRange.MISSING);
        this.index = index;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}