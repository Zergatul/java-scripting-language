package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;

import java.util.ArrayList;
import java.util.List;

public class BoundClassNode extends BoundCompilationUnitMemberNode {

    public final BoundNameExpressionNode name;
    public final List<BoundClassMemberNode> members;

    public BoundClassNode(BoundNameExpressionNode name, List<BoundClassMemberNode> members, TextRange range) {
        super(BoundNodeType.CLASS_DECLARATION, range);
        this.name = name;
        this.members = members;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
        for (BoundClassMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>(1 + members.size());
        list.add(name);
        list.addAll(members);
        return list;
    }
}