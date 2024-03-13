package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

public class BoundPredefinedTypeNode extends BoundTypeNode {
    public BoundPredefinedTypeNode(SType type, TextRange range) {
        super(NodeType.PREDEFINED_TYPE, type, range);
    }
}