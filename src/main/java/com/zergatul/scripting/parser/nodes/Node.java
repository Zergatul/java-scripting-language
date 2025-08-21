package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public abstract class Node implements Locatable {

    private final NodeType nodeType;
    private final TextRange range;

    protected Node(NodeType nodeType, TextRange range) {
        this.nodeType = nodeType;
        this.range = range;
    }

    public abstract void accept(ParserTreeVisitor visitor);
    public abstract void acceptChildren(ParserTreeVisitor visitor);

    public NodeType getNodeType() {
        return nodeType;
    }

    public TextRange getRange() {
        return this.range;
    }

    public boolean isMissing() {
        return this.range.isEmpty();
    }

    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Not implemented");
    }
}