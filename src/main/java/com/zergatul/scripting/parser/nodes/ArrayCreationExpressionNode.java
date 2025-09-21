package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ArrayCreationExpressionNode extends ExpressionNode {

    public final TypeNode typeNode;
    public final ExpressionNode lengthExpression;

    public ArrayCreationExpressionNode(TypeNode typeNode, ExpressionNode lengthExpression, TextRange range) {
        super(ParserNodeType.ARRAY_CREATION_EXPRESSION, range);
        this.typeNode = typeNode;
        this.lengthExpression = lengthExpression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
        lengthExpression.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayCreationExpressionNode other) {
            return  other.typeNode.equals(typeNode) &&
                    other.lengthExpression.equals(lengthExpression) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}