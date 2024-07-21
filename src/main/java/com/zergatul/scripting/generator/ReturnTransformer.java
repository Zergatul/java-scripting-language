package com.zergatul.scripting.generator;

import com.zergatul.scripting.binding.nodes.*;

public class ReturnTransformer {

    public BoundStatementNode process(BoundStatementNode node) {
        return transformStatement(node);
    }

    private BoundStatementNode transformStatement(BoundStatementNode statement) {
        return switch (statement.getNodeType()) {
            case BLOCK_STATEMENT -> transform((BoundBlockStatementNode) statement);
            case IF_STATEMENT -> transform((BoundIfStatementNode) statement);
            // todo: go inside loops, etc
            case RETURN_STATEMENT -> transform((BoundReturnStatementNode) statement);
            default -> statement;
        };
    }

    private BoundStatementNode transform(BoundBlockStatementNode node) {
        return new BoundBlockStatementNode(
                node.statements.stream().map(this::transformStatement).toList(),
                node.getRange());
    }

    private BoundStatementNode transform(BoundIfStatementNode node) {
        return new BoundIfStatementNode(
                node.condition,
                transformStatement(node.thenStatement),
                node.elseStatement != null ? transformStatement(node.elseStatement) : null,
                node.getRange());
    }

    private BoundStatementNode transform(BoundReturnStatementNode node) {
        return new BoundGeneratorReturnNode();
    }
}