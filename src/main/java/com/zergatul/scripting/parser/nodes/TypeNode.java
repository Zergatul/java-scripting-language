package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;

public abstract class TypeNode extends ParserNode {
    protected TypeNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}