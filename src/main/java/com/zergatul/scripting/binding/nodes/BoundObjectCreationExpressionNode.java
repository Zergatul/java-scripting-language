package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.type.ConstructorReference;

import java.util.List;

public class BoundObjectCreationExpressionNode extends BoundExpressionNode {

    public final BoundTypeNode typeNode;
    public final ConstructorReference constructor;
    public final BoundArgumentsListNode arguments;

    public BoundObjectCreationExpressionNode(BoundTypeNode typeNode, ConstructorReference constructor, BoundArgumentsListNode arguments, TextRange range) {
        super(NodeType.OBJECT_CREATION_EXPRESSION, typeNode.type, range);
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