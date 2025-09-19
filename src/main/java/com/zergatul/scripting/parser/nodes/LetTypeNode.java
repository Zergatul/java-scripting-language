package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class LetTypeNode extends TypeNode {

    public LetTypeNode(TextRange range) {
        super(NodeType.LET_TYPE, range);
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LetTypeNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}