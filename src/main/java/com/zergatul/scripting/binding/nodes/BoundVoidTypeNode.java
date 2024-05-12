package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SVoidType;

import java.util.List;

public class BoundVoidTypeNode extends BoundTypeNode {

    public BoundVoidTypeNode(TextRange range) {
        super(NodeType.VOID_TYPE, SVoidType.instance, range);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}