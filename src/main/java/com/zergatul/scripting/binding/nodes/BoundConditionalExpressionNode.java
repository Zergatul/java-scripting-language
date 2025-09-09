package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ConditionalExpressionNode;

import java.util.List;

public class BoundConditionalExpressionNode extends BoundExpressionNode {

    public final ConditionalExpressionNode syntaxNode;
    public final BoundExpressionNode condition;
    public final BoundExpressionNode whenTrue;
    public final BoundExpressionNode whenFalse;

    public BoundConditionalExpressionNode(ConditionalExpressionNode node, BoundExpressionNode condition, BoundExpressionNode whenTrue, BoundExpressionNode whenFalse) {
        this(node, condition, whenTrue, whenFalse, node.getRange());
    }

    public BoundConditionalExpressionNode(
            ConditionalExpressionNode node,
            BoundExpressionNode condition,
            BoundExpressionNode whenTrue,
            BoundExpressionNode whenFalse,
            TextRange range
    ) {
        super(BoundNodeType.CONDITIONAL_EXPRESSION, whenTrue.type, range);
        this.syntaxNode = node;
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        whenTrue.accept(visitor);
        whenFalse.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(condition, whenTrue, whenFalse);
    }
}