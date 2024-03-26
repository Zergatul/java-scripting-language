package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class InvalidTypeNode extends TypeNode {
    public InvalidTypeNode(TextRange range) {
        super(NodeType.INVALID_TYPE, range);
    }
}