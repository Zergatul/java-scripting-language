package com.zergatul.scripting;

public abstract class Node implements Locatable {

    private final NodeType nodeType;
    private final TextRange range;

    protected Node(NodeType nodeType, TextRange range) {
        this.nodeType = nodeType;
        this.range = range;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public TextRange getRange() {
        return this.range;
    }

    public boolean is(NodeType nodeType) {
        return this.nodeType == nodeType;
    }

    public boolean isNot(NodeType nodeType) {
        return this.nodeType != nodeType;
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Not implemented");
    }
}