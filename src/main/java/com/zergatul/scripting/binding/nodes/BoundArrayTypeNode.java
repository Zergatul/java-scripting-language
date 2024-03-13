package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BoundArrayTypeNode extends BoundTypeNode {

    public final BoundTypeNode underlying;

    public BoundArrayTypeNode(BoundTypeNode underlying, TextRange range) {
        super(NodeType.ARRAY_TYPE, underlying.type, range);
        this.underlying = underlying;
    }
}