package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;

public abstract class TypeNode extends Node {
    protected TypeNode(TextRange range) {
        super(range);
    }
}