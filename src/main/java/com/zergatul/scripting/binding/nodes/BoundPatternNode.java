package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;

public abstract class BoundPatternNode extends BoundNode {
    protected BoundPatternNode(BoundNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}