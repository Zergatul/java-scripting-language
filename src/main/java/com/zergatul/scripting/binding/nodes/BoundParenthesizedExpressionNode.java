package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundParenthesizedExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode inner;

    public BoundParenthesizedExpressionNode(BoundExpressionNode inner, TextRange range) {
        super(BoundNodeType.PARENTHESIZED_EXPRESSION, inner.type, range);
        this.inner = inner;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        inner.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(inner);
    }
}