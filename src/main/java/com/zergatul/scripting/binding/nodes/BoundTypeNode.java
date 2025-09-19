package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SType;

public abstract class BoundTypeNode extends BoundNode {

    public final SType type;

    protected BoundTypeNode(NodeType nodeType, SType type, TextRange range) {
        super(nodeType, range);
        this.type = type;
    }
}