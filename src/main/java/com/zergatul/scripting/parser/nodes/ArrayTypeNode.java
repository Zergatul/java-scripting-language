package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ArrayTypeNode extends TypeNode {

    public final TypeNode underlying;

    public ArrayTypeNode(TypeNode underlying, TextRange range) {
        super(ParserNodeType.ARRAY_TYPE, range);
        this.underlying = underlying;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        underlying.accept(visitor);
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