package com.zergatul.scripting.binding;

import com.zergatul.scripting.parser.nodes.*;

import java.util.Optional;

public class LambdaAnalyzer {

    public boolean canBeAction(LambdaExpressionNode lambda) {
        return switch (lambda.body.getNodeType()) {
            case INVALID_STATEMENT -> true;
            case ASSIGNMENT_STATEMENT -> true;
            case AUGMENTED_ASSIGNMENT_STATEMENT -> true;
            case INCREMENT_STATEMENT -> true;
            case DECREMENT_STATEMENT -> true;
            case EXPRESSION_STATEMENT -> true;
            case BLOCK_STATEMENT -> !hasReturnValue(lambda.body).orElse(false);
            default -> false;
        };
    }

    public boolean canBeFunction(LambdaExpressionNode lambda) {
        return switch (lambda.body.getNodeType()) {
            case INVALID_STATEMENT -> true;
            case EXPRESSION_STATEMENT -> true;
            case BLOCK_STATEMENT -> hasReturnValue(lambda.body).orElse(false);
            default -> false;
        };
    }

    private Optional<Boolean> hasReturnValue(StatementNode node) {
        return switch (node.getNodeType()) {
            case BLOCK_STATEMENT -> {
                BlockStatementNode statement = (BlockStatementNode) node;
                for (StatementNode inner : statement.statements) {
                    Optional<Boolean> result = hasReturnValue(inner);
                    if (result.isPresent()) {
                        yield result;
                    }
                }
                yield Optional.empty();
            }
            case RETURN_STATEMENT -> {
                ReturnStatementNode statement = (ReturnStatementNode) node;
                yield Optional.of(statement.expression != null);
            }
            case IF_STATEMENT -> {
                IfStatementNode statement = (IfStatementNode) node;
                yield hasReturnValue(statement.thenStatement).or(() -> {
                    if (statement.elseStatement != null) {
                        return hasReturnValue(statement.elseStatement);
                    } else {
                        return Optional.empty();
                    }
                });
            }
            case FOR_LOOP_STATEMENT -> {
                ForLoopStatementNode statement = (ForLoopStatementNode) node;
                yield hasReturnValue(statement.body);
            }
            case FOREACH_LOOP_STATEMENT -> {
                ForEachLoopStatementNode statement = (ForEachLoopStatementNode) node;
                yield hasReturnValue(statement.body);
            }
            case WHILE_LOOP_STATEMENT -> {
                WhileLoopStatementNode statement = (WhileLoopStatementNode) node;
                yield hasReturnValue(statement.body);
            }
            default -> Optional.empty();
        };
    }
}