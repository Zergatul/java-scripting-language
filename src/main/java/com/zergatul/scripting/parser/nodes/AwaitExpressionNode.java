package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class AwaitExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final ExpressionNode expression;

    public AwaitExpressionNode(Token keyword, ExpressionNode expression, TextRange range) {
        super(ParserNodeType.AWAIT_EXPRESSION, range);
        this.keyword = keyword;
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
}