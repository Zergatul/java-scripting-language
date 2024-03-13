package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SIntType;

public class BoundIntegerLiteralExpressionNode extends BoundExpressionNode {

    public final int value;

    public BoundIntegerLiteralExpressionNode(int value, TextRange range) {
        super(NodeType.INTEGER_LITERAL, SIntType.instance, range);
        this.value = value;
    }
}