package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class CustomTypeNode extends TypeNode {
    public CustomTypeNode(TextRange range) {
        super(NodeType.CUSTOM_TYPE, range);
    }
}