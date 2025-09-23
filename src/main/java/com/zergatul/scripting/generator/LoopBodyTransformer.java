package com.zergatul.scripting.generator;

import com.zergatul.scripting.binding.nodes.*;

import java.util.List;

public class LoopBodyTransformer {

    private final BoundStatementNode body;
    private final StateBoundary begin;
    private final StateBoundary end;

    public LoopBodyTransformer(BoundStatementNode body, StateBoundary begin, StateBoundary end) {
        this.body = body;
        this.begin = begin;
        this.end = end;
    }

    public BoundStatementNode process() {
        return transformStatement(body);
    }

    private BoundStatementNode transformStatement(BoundStatementNode statement) {
        return switch (statement.getNodeType()) {
            case BLOCK_STATEMENT -> transform((BoundBlockStatementNode) statement);
            case IF_STATEMENT -> transform((BoundIfStatementNode) statement);
            case BREAK_STATEMENT -> transform((BoundBreakStatementNode) statement);
            case CONTINUE_STATEMENT -> transform((BoundContinueStatementNode) statement);
            default -> statement;
        };
    }

    private BoundStatementNode transform(BoundBlockStatementNode node) {
        return new BoundBlockStatementNode(node.statements.stream().map(this::transformStatement).toList());
    }

    private BoundStatementNode transform(BoundIfStatementNode node) {
        return new BoundIfStatementNode(
                node.condition,
                transformStatement(node.thenStatement),
                node.elseStatement != null ? transformStatement(node.elseStatement) : null);
    }

    private BoundStatementNode transform(BoundBreakStatementNode node) {
        return new BoundBlockStatementNode(List.of(
                new BoundSetGeneratorStateNode(end),
                new BoundGeneratorContinueNode()));
    }

    private BoundStatementNode transform(BoundContinueStatementNode node) {
        return new BoundBlockStatementNode(List.of(
                new BoundSetGeneratorStateNode(begin),
                new BoundGeneratorContinueNode()));
    }
}