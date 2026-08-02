package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundNode;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.type.SType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoundGenericTypeNode extends BoundTypeNode {

    public final GenericTypeNode syntaxNode;
    public final BoundTypeNode rawType;
    public final BoundTypeNode[] arguments;

    public BoundGenericTypeNode(GenericTypeNode syntaxNode, BoundTypeNode rawType, BoundTypeNode[] arguments, SType type) {
        super(BoundNodeType.GENERIC_TYPE, type, syntaxNode.getRange());
        this.syntaxNode = syntaxNode;
        this.rawType = rawType;
        this.arguments = arguments;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        rawType.accept(visitor);
        for (BoundTypeNode argument : arguments) {
            argument.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> children = new ArrayList<>();
        children.add(rawType);
        Collections.addAll(children, arguments);
        return children;
    }
}