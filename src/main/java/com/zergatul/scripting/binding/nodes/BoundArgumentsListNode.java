package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;
import java.util.Objects;

public class BoundArgumentsListNode extends BoundNode {

    public final List<BoundExpressionNode> arguments;

    public BoundArgumentsListNode(List<BoundExpressionNode> arguments) {
        this(arguments, null);
    }

    public BoundArgumentsListNode(List<BoundExpressionNode> arguments, TextRange range) {
        super(BoundNodeType.ARGUMENTS_LIST, range);
        this.arguments = arguments;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundExpressionNode argument : arguments) {
            argument.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(arguments);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundArgumentsListNode other) {
            return Objects.equals(other.arguments, arguments) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}