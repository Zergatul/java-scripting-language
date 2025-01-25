package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class BoundCompilationUnitMembersListNode extends BoundNode {

    public final List<BoundCompilationUnitMemberNode> members;

    public BoundCompilationUnitMembersListNode(List<BoundCompilationUnitMemberNode> members, TextRange range) {
        super(NodeType.COMPILATION_UNIT_MEMBERS, range);
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
    public List<BoundNode> getChildren() {
        return List.copyOf(members);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundCompilationUnitMembersListNode other) {
            return Objects.equals(other.members, members);
        } else {
            return false;
        }
    }
}