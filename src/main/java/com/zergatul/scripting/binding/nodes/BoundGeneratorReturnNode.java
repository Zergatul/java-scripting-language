package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundGeneratorReturnNode extends BoundStatementNode {

    @Nullable public final BoundExpressionNode expression;

    public BoundGeneratorReturnNode(@Nullable BoundExpressionNode expression) {
        super(BoundNodeType.GENERATOR_RETURN, TextRange.MISSING);
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}
