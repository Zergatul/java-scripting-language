package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class MetaTypeOfExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;

    public MetaTypeOfExpressionNode(ExpressionNode expression, TextRange range) {
        super(NodeType.META_TYPE_OF_EXPRESSION, range);
        this.expression = expression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        throw new RuntimeException();
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        throw new RuntimeException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaTypeOfExpressionNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}