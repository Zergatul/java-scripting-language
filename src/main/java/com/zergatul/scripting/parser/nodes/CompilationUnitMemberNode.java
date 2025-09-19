package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;

public abstract class CompilationUnitMemberNode extends ParserNode {
    protected CompilationUnitMemberNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}