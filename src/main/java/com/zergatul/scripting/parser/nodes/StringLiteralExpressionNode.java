package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class StringLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public StringLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.STRING_LITERAL, range);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringLiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}