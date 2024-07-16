package com.zergatul.scripting.generator;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.visitors.AwaitVisitor;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class BinderTreeGenerator {

    public final List<StateBoundary> boundaries = new ArrayList<>();

    private StateBoundary currentBoundary;

    public void generate(List<BoundStatementNode> statements) {
        currentBoundary = newBoundary();
        for (BoundStatementNode statement : statements) {
            rewriteStatement(statement);
        }

        boolean isLastReturn = statements.get(statements.size() - 1).getNodeType() == NodeType.RETURN_STATEMENT;
        if (!isLastReturn) {
            currentBoundary.statements.add(new BoundGeneratorReturnNode());
        }
    }

    private void rewriteStatement(BoundStatementNode node) {
        if (isAsync(node)) {
            switch (node.getNodeType()) {
                case BLOCK_STATEMENT -> rewrite((BoundBlockStatementNode) node);
                case EXPRESSION_STATEMENT -> rewrite((BoundExpressionStatementNode) node);
                case IF_STATEMENT -> rewrite((BoundIfStatementNode) node);
                default -> throw new InternalException("TODO");
            }
        } else {
            markVariableDeclarations(node);
            liftCrossBoundaryVariables(node);
            currentBoundary.statements.add(node);
        }
    }

    private void rewrite(BoundBlockStatementNode node) {
        for (BoundStatementNode statement : node.statements) {
            rewriteStatement(statement);
        }
    }

    private void rewrite(BoundExpressionStatementNode node) {
        BoundExpressionNode expression = node.expression;
        switch (expression.getNodeType()) {
            case AWAIT_EXPRESSION -> {
                BoundAwaitExpressionNode awaitExpression = (BoundAwaitExpressionNode) expression;
                StateBoundary boundary = newBoundary();
                currentBoundary.statements.add(new BoundSetGeneratorStateNode(boundary));
                currentBoundary.statements.add(new BoundSetGeneratorBoundaryNode(awaitExpression.expression));
                currentBoundary = boundary;
            }
            default -> throw new InternalException("TODO");
        }
    }

    private void rewrite(BoundIfStatementNode node) {
        if (isAsync(node.condition)) {
            throw new InternalException("TODO");
        }

        StateBoundary original = currentBoundary;
        StateBoundary thenTempBoundary = new StateBoundary();
        StateBoundary elseTempBoundary = new StateBoundary();
        StateBoundary end = new StateBoundary();

        currentBoundary = thenTempBoundary;
        rewriteStatement(node.thenStatement);
        currentBoundary.statements.add(new BoundSetGeneratorStateNode(end));
        BoundBlockStatementNode thenBlock = new BoundBlockStatementNode(thenTempBoundary.statements, TextRange.EMPTY);

        BoundBlockStatementNode elseBlock;
        if (node.elseStatement != null) {
            currentBoundary = elseTempBoundary;
            rewriteStatement(node.elseStatement);
            currentBoundary.statements.add(new BoundSetGeneratorStateNode(end));
            elseBlock = new BoundBlockStatementNode(elseTempBoundary.statements, TextRange.EMPTY);
        } else {
            elseBlock = new BoundBlockStatementNode(List.of(new BoundSetGeneratorStateNode(end)), TextRange.EMPTY);
        }

        original.statements.add(new BoundIfStatementNode(
                node.condition,
                thenBlock,
                elseBlock,
                TextRange.EMPTY));

        currentBoundary = end;
        end.index = boundaries.size();
        boundaries.add(end);
    }

    private void markVariableDeclarations(BoundStatementNode statement) {
        statement.accept(new BinderTreeVisitor() {
            @Override
            public void visit(BoundVariableDeclarationNode node) {
                if (node.name.symbol instanceof LambdaLiftedLocalVariable lambdaLifted) {
                    // update lambda lifted to async lifted
                    Variable underlying = lambdaLifted.getUnderlyingVariable();
                    AsyncLiftedLocalVariable asyncLifted = new AsyncLiftedLocalVariable(underlying);
                    CapturedAsyncStateMachineFieldVariable asyncCaptured = new CapturedAsyncStateMachineFieldVariable(asyncLifted);
                    for (BoundNameExpressionNode name : underlying.getReferences()) {
                        if (name.symbol instanceof LambdaLiftedLocalVariable) {
                            name.overrideSymbol(asyncLifted);
                        }
                        if (name.symbol instanceof CapturedLocalVariable) {
                            name.overrideSymbol(asyncCaptured);
                        }
                    }
                }
                if (node.name.symbol instanceof LocalVariable local) {
                    local.setGeneratorState(currentBoundary);
                }
            }
        });
    }

    private void liftCrossBoundaryVariables(BoundStatementNode statement) {
        statement.accept(new BinderTreeVisitor() {
            @Override
            public void visit(BoundNameExpressionNode node) {
                if (node.symbol instanceof LocalVariable local) {
                    if (local.getGeneratorState() != currentBoundary) {
                        AsyncLiftedLocalVariable lifted = new AsyncLiftedLocalVariable(local);
                        for (BoundNameExpressionNode nameExpression : local.getReferences()) {
                            nameExpression.overrideSymbol(lifted);
                        }
                    }
                }
            }
        });
    }

    private StateBoundary newBoundary() {
        StateBoundary boundary = new StateBoundary(boundaries.size());
        boundaries.add(boundary);
        return boundary;
    }

    private boolean isAsync(BoundNode node) {
        AwaitVisitor visitor = new AwaitVisitor();
        node.accept(visitor);
        return visitor.isAsync();
    }
}