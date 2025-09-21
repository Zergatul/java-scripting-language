package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;

public abstract class BoundStatementNode extends BoundNode {
    protected BoundStatementNode(BoundNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}