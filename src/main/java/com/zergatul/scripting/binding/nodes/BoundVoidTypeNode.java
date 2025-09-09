package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.VoidTypeNode;
import com.zergatul.scripting.type.SVoidType;

import java.util.List;

public class BoundVoidTypeNode extends BoundTypeNode {

    public final VoidTypeNode syntaxNode;

    public BoundVoidTypeNode(VoidTypeNode node) {
        this(node, node.getRange());
    }

    public BoundVoidTypeNode(VoidTypeNode node, TextRange range) {
        super(BoundNodeType.VOID_TYPE, SVoidType.instance, range);
        this.syntaxNode = node;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}