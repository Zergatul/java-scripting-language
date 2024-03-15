package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public abstract class BoundNode implements Locatable {

    private final NodeType nodeType;
    private final TextRange range;

    protected BoundNode(NodeType nodeType, TextRange range) {
        this.nodeType = nodeType;
        this.range = range;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public TextRange getRange() {
        return this.range;
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Not implemented");
    }
}