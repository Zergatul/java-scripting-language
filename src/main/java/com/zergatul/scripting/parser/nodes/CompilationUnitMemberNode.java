package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public abstract class CompilationUnitMemberNode extends ParserNode {
    protected CompilationUnitMemberNode(ParserNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}