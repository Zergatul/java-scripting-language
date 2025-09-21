package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.SFunction;

import java.util.List;

public class BoundObjectInvocationExpression extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final BoundArgumentsListNode arguments;

    public BoundObjectInvocationExpression(BoundExpressionNode callee, BoundArgumentsListNode arguments, TextRange range) {
        super(BoundNodeType.OBJECT_INVOCATION, ((SFunction) callee.type).getReturnType(), range);
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}