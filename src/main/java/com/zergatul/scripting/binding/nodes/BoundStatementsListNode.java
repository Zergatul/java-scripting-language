package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.symbols.LiftedVariable;

import java.util.List;
import java.util.Objects;

public class BoundStatementsListNode extends BoundStatementNode {

    public final List<BoundVariableDeclarationNode> prepend;
    public final List<BoundStatementNode> statements;
    public final List<LiftedVariable> lifted;

    public BoundStatementsListNode(List<BoundStatementNode> statements) {
        this(statements, List.of(), null);
    }

    public BoundStatementsListNode(List<BoundStatementNode> statements, List<LiftedVariable> lifted, TextRange range) {
        this(List.of(), statements, lifted, range);
    }

    public BoundStatementsListNode(List<BoundVariableDeclarationNode> prepend, List<BoundStatementNode> statements, List<LiftedVariable> lifted, TextRange range) {
        super(NodeType.STATEMENTS_LIST, range);
        this.prepend = prepend;
        this.statements = statements;
        this.lifted = lifted;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundStatementNode statement : statements) {
            statement.accept(visitor);
        }
    }

    @Override
    public boolean isOpen() {
        return !statements.isEmpty() && statements.getLast().isOpen();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(statements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundStatementsListNode other) {
            return  Objects.equals(other.prepend, prepend) &&
                    Objects.equals(other.statements, statements) &&
                    Objects.equals(other.lifted, lifted) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}