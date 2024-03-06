package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class IndexExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final ExpressionNode index;

    public IndexExpressionNode(ExpressionNode callee, ExpressionNode index, TextRange range) {
        super(range);
        this.callee = callee;
        this.index = index;
    }
}