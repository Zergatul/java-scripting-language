package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;

import java.util.List;

public class BoundCompilationUnitNode extends BoundNode {

    public final CompilationUnitNode syntaxNode;
    public final BoundCompilationUnitMembersListNode members;
    public final BoundStatementsListNode statements;

    public BoundCompilationUnitNode(CompilationUnitNode node, BoundCompilationUnitMembersListNode members, BoundStatementsListNode statements) {
        this(node, members, statements, node.getRange());
    }

    public BoundCompilationUnitNode(CompilationUnitNode node, BoundCompilationUnitMembersListNode members, BoundStatementsListNode statements, TextRange range) {
        super(BoundNodeType.COMPILATION_UNIT, range);
        this.syntaxNode = node;
        this.members = members;
        this.statements = statements;
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

    public BoundCompilationUnitNode withStatements(BoundStatementsListNode statements) {
        return new BoundCompilationUnitNode(syntaxNode, members, statements, getRange());
    }
}