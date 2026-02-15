package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;

import java.util.List;

public class BoundGeneratorAddPendingFinallyStateNode extends BoundStatementNode {

    public final StateBoundary state;

    public BoundGeneratorAddPendingFinallyStateNode(StateBoundary state) {
        super(BoundNodeType.GENERATOR_ADD_PENDING_FINALLY_STATE, TextRange.MISSING);
        this.state = state;
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