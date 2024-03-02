package com.zergatul.scripting.parser.nodes;

public class IndexExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final ExpressionNode index;

    public IndexExpressionNode(ExpressionNode callee, ExpressionNode index) {
        this.callee = callee;
        this.index = index;
    }
}