package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class CustomTypeNode extends TypeNode {

    public final String value;

    public CustomTypeNode(String value, TextRange range) {
        super(NodeType.CUSTOM_TYPE, range);
        this.value = value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {

    }
}