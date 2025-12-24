package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ExtensionNode;

import java.util.ArrayList;
import java.util.List;

public class BoundExtensionNode extends BoundCompilationUnitMemberNode {

    public final ExtensionNode syntaxNode;
    public final BoundTypeNode typeNode;
    public final List<BoundExtensionMemberNode> members;

    public BoundExtensionNode(ExtensionNode node, BoundTypeNode typeNode, List<BoundExtensionMemberNode> members) {
        super(BoundNodeType.EXTENSION_DECLARATION, node.getRange());
        this.syntaxNode = node;
        this.typeNode = typeNode;
        this.members = members;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        for (BoundExtensionMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>(1 + members.size());
        list.add(typeNode);
        list.addAll(members);
        return list;
    }
}