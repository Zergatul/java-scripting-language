package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public abstract class BoundNode extends Node {

    protected BoundNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public abstract void accept(BinderTreeVisitor visitor);
    public abstract void acceptChildren(BinderTreeVisitor visitor);
    public abstract List<BoundNode> getChildren();

    public boolean isOpen() {
        return false;
    }
}