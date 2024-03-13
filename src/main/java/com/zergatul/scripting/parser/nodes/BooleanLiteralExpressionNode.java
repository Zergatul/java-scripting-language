package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class BooleanLiteralExpressionNode extends ExpressionNode {

    public final boolean value;

    public BooleanLiteralExpressionNode(boolean value, TextRange range) {
        super(NodeType.BOOLEAN_LITERAL, range);
        this.value = value;
    }
}