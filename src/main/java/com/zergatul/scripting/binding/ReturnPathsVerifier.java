package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundBlockStatementNode;
import com.zergatul.scripting.binding.nodes.BoundIfStatementNode;
import com.zergatul.scripting.binding.nodes.BoundStatementNode;

public class ReturnPathsVerifier {

    public boolean verify(BoundBlockStatementNode block) {
        for (BoundStatementNode statement : block.statements) {
            if (verify(statement)) {
                return true;
            }
        }
        return false;
    }

    private boolean verify(BoundStatementNode statement) {
        return switch (statement.getNodeType()) {
            case RETURN_STATEMENT -> true;
            case BLOCK_STATEMENT -> verify((BoundBlockStatementNode) statement);
            case IF_STATEMENT -> verify((BoundIfStatementNode) statement);
            default -> false;
        };
    }

    private boolean verify(BoundIfStatementNode statement) {
        if (statement.elseStatement == null) {
            return false;
        }
        return verify(statement.thenStatement) && verify(statement.elseStatement);
    }
}