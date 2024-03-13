package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public abstract class StatementNode extends Node {
    protected StatementNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}