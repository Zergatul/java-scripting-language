package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.type.SType;

public abstract class BoundExpressionNode extends BoundNode {

    public final SType type;

    protected BoundExpressionNode(SType type) {
        this.type = type;
    }
}