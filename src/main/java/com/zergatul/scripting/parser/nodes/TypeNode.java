package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public abstract class TypeNode extends ParserNode {
    protected TypeNode(ParserNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}