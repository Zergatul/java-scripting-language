package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

import java.util.List;
import java.util.Objects;

public class BlockStatementNode extends StatementNode {

    public final List<StatementNode> statements;

    public BlockStatementNode(List<StatementNode> statements, TextRange range) {
        super(range);
        this.statements = statements;
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