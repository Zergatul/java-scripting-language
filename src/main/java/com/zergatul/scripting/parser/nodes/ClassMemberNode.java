package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public abstract class ClassMemberNode extends ParserNode {
    protected ClassMemberNode(ParserNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}