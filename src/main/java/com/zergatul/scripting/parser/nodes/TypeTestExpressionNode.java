package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class TypeTestExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;
    public final TypeNode type;

    public TypeTestExpressionNode(ExpressionNode expression, TypeNode type, TextRange range) {
        super(NodeType.TYPE_TEST_EXPRESSION, range);
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
        if (obj instanceof TypeTestExpressionNode other) {
            return  other.expression.equals(expression) &&
                    other.type.equals(type) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}