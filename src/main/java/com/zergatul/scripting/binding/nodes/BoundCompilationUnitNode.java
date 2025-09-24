package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.EndOfFileToken;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;

import java.util.List;

public class BoundCompilationUnitNode extends BoundNode {

    public final BoundCompilationUnitMembersListNode members;
    public final BoundStatementsListNode statements;
    public final EndOfFileToken end;

    public BoundCompilationUnitNode(CompilationUnitNode node, BoundCompilationUnitMembersListNode members, BoundStatementsListNode statements) {
        this(members, statements, node.end, node.getRange());
    }

    public BoundCompilationUnitNode(BoundCompilationUnitMembersListNode members, BoundStatementsListNode statements, EndOfFileToken end, TextRange range) {
        super(BoundNodeType.COMPILATION_UNIT, range);
        this.members = members;
        this.statements = statements;
        this.end = end;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        members.accept(visitor);
        statements.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(members, statements);
    }
}