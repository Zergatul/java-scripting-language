package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundGeneratorGetValueNode extends BoundExpressionNode {

    public BoundGeneratorGetValueNode(SType type) {
        super(NodeType.GENERATOR_GET_VALUE, type, null);
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