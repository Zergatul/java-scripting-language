package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public abstract class ClassMemberNode extends Node {
    protected ClassMemberNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}