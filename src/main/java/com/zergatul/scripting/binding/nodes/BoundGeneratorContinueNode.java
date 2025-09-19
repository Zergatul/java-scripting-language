package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;

import java.util.List;

public class BoundGeneratorContinueNode extends BoundStatementNode {

    public BoundGeneratorContinueNode() {
        super(NodeType.GENERATOR_CONTINUE, null);
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