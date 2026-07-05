package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ExpressionUnitNode extends ParserNode {

    public final ExpressionNode expression;

    public ExpressionUnitNode(ExpressionNode expression) {
        super(ParserNodeType.EXPRESSION_UNIT, expression.getRange());
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
        return List.of(expression);
    }
}