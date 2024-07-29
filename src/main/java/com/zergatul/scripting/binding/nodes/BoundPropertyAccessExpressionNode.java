package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.PropertyReference;

import java.util.List;

public class BoundPropertyAccessExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final BoundPropertyNode property;

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, PropertyReference property) {
        this(callee, new BoundPropertyNode(null, property));
    }

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, BoundPropertyNode property) {
        this(callee, property, null);
    }

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, BoundPropertyNode property, TextRange range) {
        super(NodeType.PROPERTY_ACCESS_EXPRESSION, property.property.getType(), range);
        this.callee = callee;
        this.property = property;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        callee.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(callee, property);
    }
}