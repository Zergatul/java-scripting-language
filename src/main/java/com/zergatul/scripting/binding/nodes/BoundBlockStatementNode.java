package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class BoundBlockStatementNode extends BoundStatementNode {

    public final List<BoundStatementNode> statements;

    public BoundBlockStatementNode(BoundStatementNode statement1, BoundStatementNode statement2) {
        this(List.of(statement1, statement2), null);
    }

    public BoundBlockStatementNode(List<BoundStatementNode> statements) {
        this(statements, null);
    }

    public BoundBlockStatementNode(List<BoundStatementNode> statements, TextRange range) {
        super(NodeType.BLOCK_STATEMENT, range);
        this.statements = statements;
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
    public List<BoundNode> getChildren() {
        return List.copyOf(statements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundBlockStatementNode other) {
            return Objects.equals(other.statements, statements) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}