package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;

public abstract class ExpressionNode extends ParserNode {
    protected ExpressionNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }
}