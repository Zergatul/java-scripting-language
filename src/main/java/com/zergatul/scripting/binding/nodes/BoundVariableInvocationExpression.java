package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundVariableInvocationExpression extends BoundExpressionNode {

    public final BoundNameExpressionNode name;
    public final BoundArgumentsListNode arguments;

    public BoundVariableInvocationExpression(BoundNameExpressionNode name, BoundArgumentsListNode arguments, TextRange range) {
        super(NodeType.VARIABLE_INVOCATION, name.symbolRef.asVariable().asFunction().getReturnType(), range);
        this.name = name;
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