package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class InvalidExpressionNode extends ExpressionNode {

    public InvalidExpressionNode(TextRange range) {
        super(NodeType.INVALID_EXPRESSION, range);
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InvalidExpressionNode other) {
            return other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}