package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;

public class BoundGeneratorContinueNode extends BoundStatementNode {

    public BoundGeneratorContinueNode() {
        super(BoundNodeType.GENERATOR_CONTINUE, null);
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}