package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SArrayType;

public class BoundArrayTypeNode extends BoundTypeNode {

    public final BoundTypeNode underlying;

    public BoundArrayTypeNode(BoundTypeNode underlying, TextRange range) {
        super(NodeType.ARRAY_TYPE, new SArrayType(underlying.type), range);
        this.underlying = underlying;
    }
}