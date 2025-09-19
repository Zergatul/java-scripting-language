package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class IndexExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final ExpressionNode index;

    public IndexExpressionNode(ExpressionNode callee, ExpressionNode index, TextRange range) {
        super(NodeType.INDEX_EXPRESSION, range);
        this.callee = callee;
        this.index = index;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        callee.accept(visitor);
        index.accept(visitor);
    }
}