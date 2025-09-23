package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class TypeCastExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;
    public final Token keyword;
    public final TypeNode type;

    public TypeCastExpressionNode(ExpressionNode expression, Token keyword, TypeNode type) {
        super(ParserNodeType.TYPE_CAST_EXPRESSION, TextRange.combine(expression, type));
        this.expression = expression;
        this.keyword = keyword;
        this.type = type;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
        type.accept(visitor);
    }
}