package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundBlockStatementNode extends BoundStatementNode {

    public final List<BoundStatementNode> statements;

    public BoundBlockStatementNode(List<BoundStatementNode> statements, TextRange range) {
        super(NodeType.BLOCK_STATEMENT, range);
        this.statements = statements;
    }

    @Override
    public boolean isAsync() {
        for (BoundStatementNode statement : statements) {
            if (statement.isAsync()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(statements);
    }
}