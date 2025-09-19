package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;

public abstract class BoundClassMemberNode extends BoundNode {
    protected BoundClassMemberNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}