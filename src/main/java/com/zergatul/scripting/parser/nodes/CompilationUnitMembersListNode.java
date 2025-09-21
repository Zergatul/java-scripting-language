package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class CompilationUnitMembersListNode extends ParserNode {

    public final List<CompilationUnitMemberNode> members;

    public CompilationUnitMembersListNode(List<CompilationUnitMemberNode> members,  TextRange range) {
        super(ParserNodeType.COMPILATION_UNIT_MEMBERS, range);
        this.members = members;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (CompilationUnitMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompilationUnitMembersListNode other) {
            return Objects.equals(other.members, members) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}