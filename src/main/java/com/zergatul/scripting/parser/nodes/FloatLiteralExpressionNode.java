package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.FloatToken;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class FloatLiteralExpressionNode extends ExpressionNode {

    public final String value;

    public FloatLiteralExpressionNode(FloatToken token) {
        this(token.value, token.getRange());
    }

    public FloatLiteralExpressionNode(String value, TextRange range) {
        super(NodeType.FLOAT_LITERAL, range);
        this.value = value;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FloatLiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}