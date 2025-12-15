package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class IsExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;
    public final Token keyword;
    public final PatternNode pattern;

    public IsExpressionNode(ExpressionNode expression, Token keyword, PatternNode pattern) {
        super(ParserNodeType.IS_EXPRESSION, TextRange.combine(expression, pattern));
        this.expression = expression;
        this.keyword = keyword;
        this.pattern = pattern;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
        pattern.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(expression, keyword, pattern);
    }
}