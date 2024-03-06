package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;

public abstract class ExpressionNode extends Node {
    protected ExpressionNode(TextRange range) {
        super(range);
    }
}