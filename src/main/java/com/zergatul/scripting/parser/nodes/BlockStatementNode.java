package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class BlockStatementNode extends StatementNode {

    public final List<StatementNode> statements;

    public BlockStatementNode(List<StatementNode> statements, TextRange range) {
        super(ParserNodeType.BLOCK_STATEMENT, range);
        this.statements = statements;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (StatementNode statement : statements) {
            statement.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockStatementNode other) {
            return Objects.equals(other.statements, statements);
        } else {
            return false;
        }
    }
}