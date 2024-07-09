package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public abstract class ExpressionNode extends Node {

    protected ExpressionNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public abstract boolean isAsync();
}