package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SStringType;

public class BoundStringLiteralExpressionNode extends BoundExpressionNode {

    public final String value;

    public BoundStringLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.STRING_LITERAL, SStringType.instance, range);
        this.value = value;
    }
}