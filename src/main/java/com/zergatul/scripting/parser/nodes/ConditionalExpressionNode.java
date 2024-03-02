package com.zergatul.scripting.parser.nodes;

public class ConditionalExpressionNode extends ExpressionNode {

    public final ExpressionNode condition;
    public final ExpressionNode whenTrue;
    public final ExpressionNode whenFalse;

    public ConditionalExpressionNode(ExpressionNode condition, ExpressionNode whenTrue, ExpressionNode whenFalse) {
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }
}