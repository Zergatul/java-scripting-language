package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public abstract class BoundStatementNode extends BoundNode {

    protected BoundStatementNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public abstract boolean isAsync();
}