package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class FloatLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public FloatLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.FLOAT_LITERAL, range);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloatLiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}