package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.List;
import java.util.Objects;

public class BoundCompilationUnitMembersListNode extends BoundNode {

    public final List<BoundCompilationUnitMemberNode> members;

    public BoundCompilationUnitMembersListNode(List<BoundCompilationUnitMemberNode> members, TextRange range) {
        super(BoundNodeType.COMPILATION_UNIT_MEMBERS, range);
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