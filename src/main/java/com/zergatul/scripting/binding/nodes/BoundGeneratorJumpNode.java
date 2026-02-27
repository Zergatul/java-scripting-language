package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundGeneratorJumpNode extends BoundStatementNode {

    public final StateBoundary state;
    public final @Nullable StateBoundary finallyEpilogue;

    // when inside finally-block, finallyEpilogue is not null
    public BoundGeneratorJumpNode(StateBoundary state, @Nullable StateBoundary finallyEpilogue) {
        super(BoundNodeType.GENERATOR_JUMP, TextRange.MISSING);
        this.state = state;
        this.finallyEpilogue = finallyEpilogue;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}