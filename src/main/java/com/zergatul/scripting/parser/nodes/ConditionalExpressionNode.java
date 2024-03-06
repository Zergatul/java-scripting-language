package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class ConditionalExpressionNode extends ExpressionNode {

    public final ExpressionNode condition;
    public final ExpressionNode whenTrue;
    public final ExpressionNode whenFalse;

    public ConditionalExpressionNode(ExpressionNode condition, ExpressionNode whenTrue, ExpressionNode whenFalse, TextRange range) {
        super(range);
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }
}