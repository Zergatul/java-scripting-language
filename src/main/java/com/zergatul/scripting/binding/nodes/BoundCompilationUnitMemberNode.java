package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;

public abstract class BoundCompilationUnitMemberNode extends BoundNode {
    protected BoundCompilationUnitMemberNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}