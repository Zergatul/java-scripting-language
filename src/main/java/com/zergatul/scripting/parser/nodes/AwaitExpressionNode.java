package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class AwaitExpressionNode extends ExpressionNode {

    public final Token awaitToken;
    public final ExpressionNode expression;

    public AwaitExpressionNode(Token awaitToken, ExpressionNode expression, TextRange range) {
        super(ParserNodeType.AWAIT_EXPRESSION, range);
        this.awaitToken = awaitToken;
        this.expression = expression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AwaitExpressionNode other) {
            return other.expression.equals(expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}