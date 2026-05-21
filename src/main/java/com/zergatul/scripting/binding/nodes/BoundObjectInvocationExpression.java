package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.SFunction;

import java.util.List;

public class BoundObjectInvocationExpression extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final SFunction callableType;
    public final BoundArgumentsListNode arguments;

    public BoundObjectInvocationExpression(
            BoundExpressionNode callee,
            SFunction callableType,
            BoundArgumentsListNode arguments,
            TextRange range
    ) {
        super(BoundNodeType.OBJECT_INVOCATION, callableType.getReturnType(), range);
        this.callee = callee;
        this.callableType = callableType;
        this.arguments = arguments;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        callee.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(callee, arguments);
    }
}