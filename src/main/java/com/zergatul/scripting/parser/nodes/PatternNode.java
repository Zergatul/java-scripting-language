package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public abstract class PatternNode extends ParserNode {
    protected PatternNode(ParserNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}