package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;

public abstract class BoundCompilationUnitMemberNode extends BoundNode {
    protected BoundCompilationUnitMemberNode(BoundNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}