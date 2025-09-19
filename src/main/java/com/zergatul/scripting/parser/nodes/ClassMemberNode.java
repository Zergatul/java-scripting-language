package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;

public abstract class ClassMemberNode extends ParserNode {
    protected ClassMemberNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}