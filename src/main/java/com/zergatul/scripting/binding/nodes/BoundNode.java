package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.ArrayList;
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

    public List<BoundNode> find(int line, int column) {
        List<BoundNode> chain = new ArrayList<>();
        findInternal(chain, line, column);
        return chain.reversed();
    }

    private void findInternal(List<BoundNode> chain, int line, int column) {
        if (getRange().contains(line, column)) {
            chain.add(this);
            for (BoundNode child : getChildren()) {
                if (child.getRange().contains(line, column)) {
                    child.findInternal(chain, line, column);
                    return;
                }
            }
        }
    }
}