package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class NameExpressionNode extends ExpressionNode {

    public final String value;

    public NameExpressionNode(ValueToken identifier) {
        this(identifier.value, identifier.getRange());
    }

    public NameExpressionNode(String value, TextRange range) {
        super(NodeType.NAME_EXPRESSION, range);
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
        if (obj instanceof NameExpressionNode other) {
            return other.value.equals(value) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}