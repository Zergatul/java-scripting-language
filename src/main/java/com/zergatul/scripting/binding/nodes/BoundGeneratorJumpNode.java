package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;

import java.util.List;

public class BoundGeneratorJumpNode extends BoundStatementNode {

    public final StateBoundary state;

    public BoundGeneratorJumpNode(StateBoundary state) {
        super(BoundNodeType.GENERATOR_JUMP, TextRange.MISSING);
        this.state = state;
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