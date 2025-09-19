package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SType;

public abstract class BoundExpressionNode extends BoundNode {

    public final SType type;

    protected BoundExpressionNode(NodeType nodeType, SType type, TextRange range) {
        super(nodeType, range);
        this.type = type;
    }

    public boolean canGet() {
        return true;
    }

    public boolean canSet() {
        return false;
    }

    protected boolean equals(BoundExpressionNode node1, BoundExpressionNode node2) {
        return node1.type.equals(node2.type) && node1.getRange().equals(node2.getRange());
    }
}