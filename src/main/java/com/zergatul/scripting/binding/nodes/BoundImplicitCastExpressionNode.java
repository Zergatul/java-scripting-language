package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.operation.CastOperation;

import java.util.List;

public class BoundImplicitCastExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode operand;
    public final CastOperation operation;

    public BoundImplicitCastExpressionNode(BoundExpressionNode operand, CastOperation operation) {
        this(operand, operation, TextRange.MISSING);
    }

    public BoundImplicitCastExpressionNode(BoundExpressionNode operand, CastOperation operation, TextRange range) {
        super(BoundNodeType.IMPLICIT_CAST, operation.type, range);
        this.operand = operand;
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        operand.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(operand);
    }
}