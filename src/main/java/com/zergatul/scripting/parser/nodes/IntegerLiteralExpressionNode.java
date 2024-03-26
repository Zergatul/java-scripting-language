package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.IntegerToken;
import com.zergatul.scripting.parser.NodeType;

public class IntegerLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public IntegerLiteralExpressionNode(IntegerToken token) {
        this(token.value, token.getRange());
    }

    public IntegerLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.INTEGER_LITERAL, range);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerLiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}