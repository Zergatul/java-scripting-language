package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ConstructorInitializerNode;
import com.zergatul.scripting.type.ConstructorReference;

import java.util.List;

public class BoundConstructorInitializerNode extends BoundNode {

    public final ConstructorInitializerNode syntaxNode;
    public final BoundArgumentsListNode arguments;
    public final ConstructorReference constructor;

    public BoundConstructorInitializerNode(
            ConstructorInitializerNode node,
            BoundArgumentsListNode arguments,
            ConstructorReference constructor
    ) {
        this(node, arguments, constructor, node.getRange());
    }

    public BoundConstructorInitializerNode(
            ConstructorInitializerNode node,
            BoundArgumentsListNode arguments,
            ConstructorReference constructor,
            TextRange range
    ) {
        super(BoundNodeType.CONSTRUCTOR_INITIALIZER, range);
        this.syntaxNode = node;
        this.arguments = arguments;
        this.constructor = constructor;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        arguments.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(arguments);
    }
}