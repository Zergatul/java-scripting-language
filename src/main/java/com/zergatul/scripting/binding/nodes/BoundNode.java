package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public abstract class BoundNode implements Locatable {

    private final NodeType nodeType;
    private final TextRange range;

    protected BoundNode(NodeType nodeType, TextRange range) {
        this.nodeType = nodeType;
        this.range = range;
    }

    public abstract void accept(BinderTreeVisitor visitor);
    public abstract void acceptChildren(BinderTreeVisitor visitor);

    public NodeType getNodeType() {
        return nodeType;
    }

    public TextRange getRange() {
        return this.range;
    }

    public abstract List<BoundNode> getChildren();

    @Override
    public boolean equals(Object obj) {
        throw new InternalException("Not implemented");
    }
}