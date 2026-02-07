package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ThrowExpressionNode;
import com.zergatul.scripting.type.SNever;

import java.util.List;

public class BoundThrowExpressionNode extends BoundExpressionNode {

    public final ThrowExpressionNode syntaxNode;
    public final BoundExpressionNode expression;

    public BoundThrowExpressionNode(ThrowExpressionNode node, BoundExpressionNode expression) {
        super(BoundNodeType.THROW_EXPRESSION, SNever.instance, node.getRange());
        this.syntaxNode = node;
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