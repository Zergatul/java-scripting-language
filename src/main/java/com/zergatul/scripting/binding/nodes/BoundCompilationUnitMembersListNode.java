package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CompilationUnitMembersListNode;

import java.util.List;

public class BoundCompilationUnitMembersListNode extends BoundNode {

    public final CompilationUnitMembersListNode syntaxNode;
    public final List<BoundCompilationUnitMemberNode> members;

    public BoundCompilationUnitMembersListNode(CompilationUnitMembersListNode node, List<BoundCompilationUnitMemberNode> members) {
        this(node, members, node.getRange());
    }

    public BoundCompilationUnitMembersListNode(CompilationUnitMembersListNode node, List<BoundCompilationUnitMemberNode> members, TextRange range) {
        super(BoundNodeType.COMPILATION_UNIT_MEMBERS, range);
        this.syntaxNode = node;
        this.members = members;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundCompilationUnitMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public boolean isOpen() {
        return !members.isEmpty() && members.getLast().isOpen();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(members);
    }
}