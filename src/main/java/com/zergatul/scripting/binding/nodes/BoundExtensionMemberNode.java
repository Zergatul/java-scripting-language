package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;

public abstract class BoundExtensionMemberNode extends BoundNode {
    protected BoundExtensionMemberNode(BoundNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}