package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.operation.BinaryOperation;

import java.util.List;

public class BoundBinaryOperatorNode extends BoundNode {

    public final BinaryOperation operation;

    public BoundBinaryOperatorNode(BinaryOperation operation) {
        this(operation, null);
    }

    public BoundBinaryOperatorNode(BinaryOperation operation, TextRange range) {
        super(BoundNodeType.BINARY_OPERATOR, range);
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}