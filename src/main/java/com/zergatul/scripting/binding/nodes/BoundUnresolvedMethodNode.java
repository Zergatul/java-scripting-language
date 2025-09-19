package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;

import java.util.List;

public class BoundUnresolvedMethodNode extends BoundNode {

    public final String name;

    public BoundUnresolvedMethodNode(String name, TextRange range) {
        super(NodeType.UNRESOLVED_METHOD, range);
        this.name = name;
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