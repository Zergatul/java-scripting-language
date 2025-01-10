package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.MethodReference;

import java.util.List;

public class BoundMethodNode extends BoundNode {

    public final MethodReference method;

    public BoundMethodNode(MethodReference method, TextRange range) {
        super(NodeType.METHOD, range);
        this.method = method;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundMethodNode other) {
            return other.method.equals(method) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}