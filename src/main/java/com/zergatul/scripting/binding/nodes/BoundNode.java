package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public abstract class BoundNode implements Locatable {

    private final BoundNodeType nodeType;
    private final TextRange range;

    protected BoundNode(BoundNodeType nodeType, TextRange range) {
        this.nodeType = nodeType;
        this.range = range;
    }

    public BoundNodeType getNodeType() {
        return nodeType;
    }

    public TextRange getRange() {
        return this.range;
    }

    public boolean is(BoundNodeType nodeType) {
        return this.nodeType == nodeType;
    }

    public boolean isNot(BoundNodeType nodeType) {
        return this.nodeType != nodeType;
    }

    public abstract void accept(BinderTreeVisitor visitor);
    public abstract void acceptChildren(BinderTreeVisitor visitor);
    public abstract List<BoundNode> getChildren();

    public boolean isOpen() {
        return false;
    }
}