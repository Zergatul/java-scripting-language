package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class ArrayTypeNode extends TypeNode {

    public final TypeNode underlying;

    public ArrayTypeNode(TypeNode underlying, TextRange range) {
        super(range);
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