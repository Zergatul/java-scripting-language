package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundRefTypeNode extends BoundTypeNode {

    public final BoundTypeNode underlying;

    public BoundRefTypeNode(BoundTypeNode underlying, SType type, TextRange range) {
        super(NodeType.REF_TYPE, type, range);
        this.underlying = underlying;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(underlying);
    }
}
