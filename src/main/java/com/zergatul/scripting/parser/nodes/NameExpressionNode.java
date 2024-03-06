package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.IdentifierToken;

public class NameExpressionNode extends ExpressionNode {

    public final String value;

    public NameExpressionNode(IdentifierToken identifier) {
        this(identifier.value, identifier.getRange());
    }

    public NameExpressionNode(String value, TextRange range) {
        super(range);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NameExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}