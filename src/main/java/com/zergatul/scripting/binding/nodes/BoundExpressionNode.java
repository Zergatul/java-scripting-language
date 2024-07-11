package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

public abstract class BoundExpressionNode extends BoundNode {

    public final SType type;

    protected BoundExpressionNode(NodeType nodeType, SType type, TextRange range) {
        super(nodeType, range);
        this.type = type;
    }

    public abstract boolean isAsync();

    public boolean canGet() {
        return true;
    }

    public boolean canSet() {
        return false;
    }
}