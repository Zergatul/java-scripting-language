package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.type.PropertyReference;

import java.util.List;

public class BoundPropertyNode extends BoundNode {

    public final String name;
    public final PropertyReference property;

    public BoundPropertyNode(String name, PropertyReference property) {
        this(name, property, null);
    }

    public BoundPropertyNode(String name, PropertyReference property, TextRange range) {
        super(BoundNodeType.PROPERTY, range);
        this.name = name;
        this.property = property;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}