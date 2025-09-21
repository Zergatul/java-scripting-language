package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class UnaryExpressionNode extends ExpressionNode {

    public final UnaryOperatorNode operator;
    public final ExpressionNode operand;

    public UnaryExpressionNode(UnaryOperatorNode operator, ExpressionNode operand, TextRange range) {
        super(ParserNodeType.UNARY_EXPRESSION, range);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        operator.accept(visitor);
        operand.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnaryExpressionNode other) {
            return other.operator.equals(operator) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}