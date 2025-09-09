package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public abstract class ExpressionNode extends ParserNode {
    protected ExpressionNode(ParserNodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}