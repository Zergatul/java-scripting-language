package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class JavaQualifiedTypeNameNode extends ParserNode {

    public final String value;

    public JavaQualifiedTypeNameNode(String value, TextRange range) {
        super(NodeType.JAVA_TYPE_NAME, range);
        this.value = value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaQualifiedTypeNameNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}