package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class VoidTypeNode extends TypeNode {

    public VoidTypeNode(TextRange range) {
        super(NodeType.VOID_TYPE, range);
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VoidTypeNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}