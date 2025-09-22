package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ConditionalExpressionNode extends ExpressionNode {

    public final ExpressionNode condition;
    public final Token questionMark;
    public final ExpressionNode whenTrue;
    public final Token colon;
    public final ExpressionNode whenFalse;

    public ConditionalExpressionNode(
            ExpressionNode condition,
            Token questionMark,
            ExpressionNode whenTrue,
            Token colon,
            ExpressionNode whenFalse,
            TextRange range
    ) {
        super(ParserNodeType.CONDITIONAL_EXPRESSION, range);
        this.condition = condition;
        this.questionMark = questionMark;
        this.whenTrue = whenTrue;
        this.colon = colon;
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