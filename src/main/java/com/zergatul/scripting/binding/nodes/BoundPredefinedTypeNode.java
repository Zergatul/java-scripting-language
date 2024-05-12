package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundPredefinedTypeNode extends BoundTypeNode {

    public BoundPredefinedTypeNode(SType type, TextRange range) {
        super(NodeType.PREDEFINED_TYPE, type, range);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}