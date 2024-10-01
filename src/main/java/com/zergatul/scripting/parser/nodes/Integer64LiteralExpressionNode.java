package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Integer64Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class Integer64LiteralExpressionNode extends ExpressionNode {

    public final String value;

    public Integer64LiteralExpressionNode(Integer64Token token) {
        this(token.value, token.getRange());
    }

    public Integer64LiteralExpressionNode(String value, TextRange range) {
        super(NodeType.INTEGER64_LITERAL, range);
        this.value = value;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Integer64LiteralExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}