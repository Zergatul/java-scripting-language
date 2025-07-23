package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class JavaTypeNode extends TypeNode {

    public final String type;

    public JavaTypeNode(String type, TextRange range) {
        super(NodeType.JAVA_TYPE, range);
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
        if (obj instanceof JavaTypeNode other) {
            return other.type.equals(type) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}