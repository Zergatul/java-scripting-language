package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class CompilationUnitNode extends Node {

    public final CompilationUnitMembersListNode members;
    public final StatementsListNode statements;

    public CompilationUnitNode(CompilationUnitMembersListNode members, StatementsListNode statements, TextRange range) {
        super(NodeType.COMPILATION_UNIT, range);
        this.members = members;
        this.statements = statements;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        members.accept(visitor);
        statements.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CompilationUnitNode other) {
            return  other.members.equals(members) &&
                    other.statements.equals(statements) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}