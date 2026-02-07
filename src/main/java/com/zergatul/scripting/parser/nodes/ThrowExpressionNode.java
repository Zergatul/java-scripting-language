package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ThrowExpressionNode extends ExpressionNode {

    public final Token keyword;
    public final ExpressionNode expression;

    public ThrowExpressionNode(Token keyword, ExpressionNode expression) {
        super(ParserNodeType.THROW_EXPRESSION, TextRange.combine(keyword, expression));
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

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, expression);
    }
}