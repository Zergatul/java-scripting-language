package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class UnaryExpressionNode extends ExpressionNode {

    public final UnaryOperatorNode operator;
    public final ExpressionNode operand;

    public UnaryExpressionNode(UnaryOperatorNode operator, ExpressionNode operand) {
        super(ParserNodeType.UNARY_EXPRESSION, TextRange.combine(operator, operand));
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
    public List<Locatable> getChildNodes() {
        return List.of(operand);
    }
}