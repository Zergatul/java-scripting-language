package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundCompilationUnitNode extends BoundNode {

    public final BoundCompilationUnitMembersListNode members;
    public final BoundStatementsListNode statements;

    public BoundCompilationUnitNode(BoundCompilationUnitMembersListNode members, BoundStatementsListNode statements, TextRange range) {
        super(NodeType.COMPILATION_UNIT, range);
        this.members = members;
        this.statements = statements;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
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