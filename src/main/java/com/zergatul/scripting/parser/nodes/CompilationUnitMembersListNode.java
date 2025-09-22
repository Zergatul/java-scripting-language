package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

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
}