package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class BooleanLiteralExpressionNode extends ExpressionNode {

    public final boolean value;

    public BooleanLiteralExpressionNode(boolean value, TextRange range) {
        super(NodeType.BOOLEAN_LITERAL, range);
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
        if (obj instanceof BooleanLiteralExpressionNode other) {
            return other.value == value && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}