package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;

import java.util.List;

public class BoundGeneratorAwaitTransitionNode extends BoundStatementNode {

    public final BoundExpressionNode expression;
    public final StateBoundary resumeState;
    public final StateBoundary catchState;

    public BoundGeneratorAwaitTransitionNode(BoundExpressionNode expression, StateBoundary resumeState, StateBoundary catchState) {
        super(BoundNodeType.GENERATOR_AWAIT_TRANSITION, TextRange.MISSING);
        this.expression = expression;
        this.resumeState = resumeState;
        this.catchState = catchState;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}