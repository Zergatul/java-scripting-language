package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;

public abstract class BoundClassMemberNode extends BoundNode {
    protected BoundClassMemberNode(BoundNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}