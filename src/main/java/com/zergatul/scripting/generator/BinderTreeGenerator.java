package com.zergatul.scripting.generator;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.AsyncBinderTreeVisitor;
import com.zergatul.scripting.compiler.*;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.NodeType;

import java.util.ArrayList;
import java.util.List;

public class BinderTreeGenerator {

    public final List<StateBoundary> boundaries = new ArrayList<>();

    private StateBoundary currentBoundary;

    public void generate(List<BoundStatementNode> statements) {
        currentBoundary = newBoundary();
        for (BoundStatementNode statement : statements) {
            if (isAsync(statement)) {
                switch (statement.getNodeType()) {
                    case EXPRESSION_STATEMENT -> rewrite((BoundExpressionStatementNode) statement);
                    default -> throw new InternalException();
                }
            } else {
                markVariableDeclarations(statement);
                liftCrossBoundaryVariables(statement);
                currentBoundary.statements.add(statement);
            }
        }

        boolean isLastReturn = statements.get(statements.size() - 1).getNodeType() == NodeType.RETURN_STATEMENT;
        if (!isLastReturn) {
            currentBoundary.statements.add(new BoundGeneratorReturnNode());
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
            default -> throw new InternalException();
        }
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
        AsyncBinderTreeVisitor visitor = new AsyncBinderTreeVisitor();
        node.accept(visitor);
        return visitor.isAsync();
    }
}