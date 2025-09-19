package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.PropertyReference;

import java.util.List;

public class BoundPropertyAccessExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final Token dot;
    public final BoundPropertyNode property;

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, PropertyReference property) {
        this(callee, null, new BoundPropertyNode(null, property), null);
    }

    public BoundPropertyAccessExpressionNode(BoundExpressionNode callee, Token dot, BoundPropertyNode property, TextRange range) {
        super(NodeType.PROPERTY_ACCESS_EXPRESSION, property.property.getType(), range);
        this.callee = callee;
        this.dot = dot;
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