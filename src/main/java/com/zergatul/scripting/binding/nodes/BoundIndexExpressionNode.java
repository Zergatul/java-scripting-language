package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.operation.IndexOperation;

import java.util.List;

public class BoundIndexExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final BoundExpressionNode index;
    public final IndexOperation operation;

    public BoundIndexExpressionNode(BoundExpressionNode callee, BoundExpressionNode index, IndexOperation operation) {
        this(callee, index, operation, null);
    }

    public BoundIndexExpressionNode(BoundExpressionNode callee, BoundExpressionNode index, IndexOperation operation, TextRange range) {
        super(BoundNodeType.INDEX_EXPRESSION, operation.returnType, range);
        this.callee = callee;
        this.index = index;
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        callee.accept(visitor);
        index.accept(visitor);
    }

    @Override
    public boolean canSet() {
        return operation.canSet();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(callee, index);
    }
}