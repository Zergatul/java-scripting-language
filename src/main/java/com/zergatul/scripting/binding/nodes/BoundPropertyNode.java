package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.type.PropertyReference;

import java.util.List;

public class BoundPropertyNode extends BoundNode {

    public final NameExpressionNode syntaxNode;
    public final String name;
    public final PropertyReference property;

    public BoundPropertyNode(PropertyReference property) {
        this(SyntaxFactory.missingNameExpression(), "", property, TextRange.MISSING);
    }

    public BoundPropertyNode(NameExpressionNode node, PropertyReference property) {
        this(node, node.value, property, node.getRange());
    }

    public BoundPropertyNode(NameExpressionNode node, String name, PropertyReference property, TextRange range) {
        super(BoundNodeType.PROPERTY, range);
        this.syntaxNode = node;
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