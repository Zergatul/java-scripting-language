package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.PredefinedType;

public class PredefinedTypeNode extends TypeNode {

    public final PredefinedType type;

    public PredefinedTypeNode(PredefinedType type, TextRange range) {
        super(ParserNodeType.PREDEFINED_TYPE, range);
        this.type = type;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PredefinedTypeNode other) {
            return other.type == type;
        } else {
            return false;
        }
    }
}