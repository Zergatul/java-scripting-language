package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;

public abstract class StatementNode extends Node {
    protected StatementNode(TextRange range) {
        super(range);
    }
}