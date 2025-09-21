package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class BinaryExpressionNode extends ExpressionNode {

    public final ExpressionNode left;
    public final BinaryOperatorNode operator;
    public final ExpressionNode right;

    public BinaryExpressionNode(ExpressionNode left, BinaryOperatorNode operator, ExpressionNode right, TextRange range) {
        super(ParserNodeType.BINARY_EXPRESSION, range);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        left.accept(visitor);
        operator.accept(visitor);
        right.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryExpressionNode other) {
            return other.left.equals(left) && other.operator.equals(operator) && other.right.equals(right);
        } else {
            return false;
        }
    }
}