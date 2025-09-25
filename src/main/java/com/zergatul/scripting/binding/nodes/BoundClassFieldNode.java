package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassFieldNode;

import java.util.List;

public class BoundClassFieldNode extends BoundClassMemberNode {

    public final ClassFieldNode syntaxNode;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;

    public BoundClassFieldNode(ClassFieldNode node, BoundTypeNode typeNode, BoundNameExpressionNode name) {
        this(node, typeNode, name, node.getRange());
    }

    public BoundClassFieldNode(ClassFieldNode node, BoundTypeNode typeNode, BoundNameExpressionNode name, TextRange range) {
        super(BoundNodeType.CLASS_FIELD, range);
        this.syntaxNode = node;
        this.typeNode = typeNode;
        this.name = name;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        name.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, name);
    }
}