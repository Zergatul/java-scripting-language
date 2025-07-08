package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class TypePatternNode extends PatternNode {

    public final TypeNode type;

    public TypePatternNode(TypeNode type, TextRange range) {
        super(NodeType.TYPE_PATTERN, range);
        this.type = type;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypePatternNode other) {
            return other.type.equals(type) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}