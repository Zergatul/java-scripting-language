package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;

import java.util.List;

public class BoundSetGeneratorStateNode extends BoundStatementNode {

    public final StateBoundary boundary;

    public BoundSetGeneratorStateNode(StateBoundary boundary) {
        super(BoundNodeType.SET_GENERATOR_STATE, null);
        this.boundary = boundary;
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
