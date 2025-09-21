package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class TypeCastExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;
    public final TypeNode type;

    public TypeCastExpressionNode(ExpressionNode expression, TypeNode type, TextRange range) {
        super(ParserNodeType.TYPE_CAST_EXPRESSION, range);
        this.expression = expression;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeCastExpressionNode other) {
            return  other.expression.equals(expression) &&
                    other.type.equals(type) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}