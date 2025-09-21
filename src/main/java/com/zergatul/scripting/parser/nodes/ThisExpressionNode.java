package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ThisExpressionNode extends ExpressionNode {

    public ThisExpressionNode(TextRange range) {
        super(ParserNodeType.THIS_EXPRESSION, range);
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}