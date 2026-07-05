package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundExpressionUnitNode extends BoundNode {

    public final BoundExpressionNode expression;

    public BoundExpressionUnitNode(BoundExpressionNode expression) {
        super(BoundNodeType.EXPRESSION_UNIT, expression.getRange());
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}