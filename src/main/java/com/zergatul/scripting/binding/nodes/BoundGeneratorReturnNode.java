package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundGeneratorReturnNode extends BoundStatementNode {

    public final @Nullable BoundExpressionNode expression;
    public final @Nullable StateBoundary pendingState;

    public BoundGeneratorReturnNode(@Nullable BoundExpressionNode expression, @Nullable StateBoundary pendingState) {
        super(BoundNodeType.GENERATOR_RETURN, TextRange.MISSING);
        this.expression = expression;
        this.pendingState = pendingState;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}
