package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ConditionalExpressionNode extends ExpressionNode {

    public final ExpressionNode condition;
    public final ExpressionNode whenTrue;
    public final ExpressionNode whenFalse;

    public ConditionalExpressionNode(ExpressionNode condition, ExpressionNode whenTrue, ExpressionNode whenFalse, TextRange range) {
        super(ParserNodeType.CONDITIONAL_EXPRESSION, range);
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        condition.accept(visitor);
        whenTrue.accept(visitor);
        whenFalse.accept(visitor);
    }
}