package com.zergatul.scripting.parser.nodes;

public class ArrayTypeNode extends TypeNode {

    public final TypeNode underlying;

    public ArrayTypeNode(TypeNode underlying) {
        this.underlying = underlying;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayTypeNode other) {
            return other.underlying.equals(underlying);
        } else {
            return false;
        }
    }
}