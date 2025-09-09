package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ObjectCreationExpressionNode;
import com.zergatul.scripting.type.ConstructorReference;

import java.util.List;

public class BoundObjectCreationExpressionNode extends BoundExpressionNode {

    public final ObjectCreationExpressionNode syntaxNode;
    public final BoundTypeNode typeNode;
    public final ConstructorReference constructor;
    public final BoundArgumentsListNode arguments;

    public BoundObjectCreationExpressionNode(ObjectCreationExpressionNode node, BoundTypeNode typeNode, ConstructorReference constructor, BoundArgumentsListNode arguments) {
        this(node, typeNode, constructor, arguments, node.getRange());
    }

    public BoundObjectCreationExpressionNode(ObjectCreationExpressionNode node, BoundTypeNode typeNode, ConstructorReference constructor, BoundArgumentsListNode arguments, TextRange range) {
        super(BoundNodeType.OBJECT_CREATION_EXPRESSION, typeNode.type, range);
        this.syntaxNode = node;
        this.typeNode = typeNode;
        this.constructor = constructor;
        this.arguments = arguments;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, arguments);
    }
}