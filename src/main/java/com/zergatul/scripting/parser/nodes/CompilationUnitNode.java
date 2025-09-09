package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.EndOfFileToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class CompilationUnitNode extends ParserNode {

    public final CompilationUnitMembersListNode members;
    public final StatementsListNode statements;
    public final EndOfFileToken end;

    public CompilationUnitNode(CompilationUnitMembersListNode members, StatementsListNode statements, EndOfFileToken end) {
        super(ParserNodeType.COMPILATION_UNIT, TextRange.combine(members, statements));
        this.members = members;
        this.statements = statements;
        this.end = end;
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
    public List<Locatable> getChildNodes() {
        return List.of(members, statements, end);
    }
}