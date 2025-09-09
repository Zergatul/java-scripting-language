package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;
import com.zergatul.scripting.type.PropertyReference;

import java.util.List;

public class BoundPropertyAccessExpressionNode extends BoundExpressionNode {

    public final MemberAccessExpressionNode syntaxNode;
    public final BoundExpressionNode callee;
    public final BoundPropertyNode property;

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, PropertyReference property) {
        this(null, callee, new BoundPropertyNode(property), null);
    }

    public BoundPropertyAccessExpressionNode(MemberAccessExpressionNode node, BoundExpressionNode callee, BoundPropertyNode property) {
        this(node, callee, property, node.getRange());
    }

    public BoundPropertyAccessExpressionNode(MemberAccessExpressionNode node, BoundExpressionNode callee, BoundPropertyNode property, TextRange range) {
        super(BoundNodeType.PROPERTY_ACCESS_EXPRESSION, property.property.getType(), range);
        this.syntaxNode = node;
        this.callee = callee;
        this.property = property;
    }

    @Override
    public boolean canGet() {
        return property.property.canGet();
    }

    @Override
    public boolean canSet() {
        return property.property.canSet();
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