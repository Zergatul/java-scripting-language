package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidTypeNode extends TypeNode {

    public InvalidTypeNode(TextRange range) {
        super(ParserNodeType.INVALID_TYPE, range);
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvalidTypeNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}