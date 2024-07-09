package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class StatementsListNode extends StatementNode {

    public final List<StatementNode> statements;

    public StatementsListNode(List<StatementNode> statements, TextRange range) {
        super(NodeType.STATEMENTS_LIST, range);
        this.statements = statements;
    }

    @Override
    public boolean isAsync() {
        for (StatementNode statement : statements) {
            if (statement.isAsync()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatementsListNode other) {
            return Objects.equals(other.statements, statements) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}