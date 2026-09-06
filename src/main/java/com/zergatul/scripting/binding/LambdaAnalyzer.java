package com.zergatul.scripting.binding;

import com.zergatul.scripting.parser.nodes.*;

import java.util.Optional;

public class LambdaAnalyzer {

    public boolean canBeAction(LambdaExpressionNode lambda) {
        switch (lambda.body.getNodeType()) {
            case INVALID_STATEMENT:
            case ASSIGNMENT_STATEMENT:
            case AUGMENTED_ASSIGNMENT_STATEMENT:
            case INCREMENT_STATEMENT:
            case DECREMENT_STATEMENT:
            case EXPRESSION_STATEMENT:
                return true;

            case BLOCK_STATEMENT:
                return !hasReturnValue(lambda.body).orElse(false);

            default:
                return false;
        }
    }

    public boolean canBeFunction(LambdaExpressionNode lambda) {
        switch (lambda.body.getNodeType()) {
            case INVALID_STATEMENT:
            case EXPRESSION_STATEMENT:
                return true;

            case BLOCK_STATEMENT:
                return hasReturnValue(lambda.body).orElse(false);

            default:
                return false;
        }
    }

    private Optional<Boolean> hasReturnValue(StatementNode node) {
        switch (node.getNodeType()) {
            case BLOCK_STATEMENT:
                BlockStatementNode statement1 = (BlockStatementNode) node;
                for (StatementNode inner : statement1.statements) {
                    Optional<Boolean> result = hasReturnValue(inner);
                    if (result.isPresent()) {
                        return result;
                    }
                }
                return Optional.empty();

            case RETURN_STATEMENT:
                ReturnStatementNode statement2 = (ReturnStatementNode) node;
                return Optional.of(statement2.expression != null);

            case IF_STATEMENT:
                IfStatementNode statement3 = (IfStatementNode) node;
                Optional<Boolean> optional = hasReturnValue(statement3.thenStatement);
                if (optional.isPresent()) {
                    return optional;
                }
                if (statement3.elseStatement != null) {
                    return hasReturnValue(statement3.elseStatement);
                } else {
                    return Optional.empty();
                }

            case FOR_LOOP_STATEMENT:
                ForLoopStatementNode statement4 = (ForLoopStatementNode) node;
                return hasReturnValue(statement4.body);

            case FOREACH_LOOP_STATEMENT:
                ForEachLoopStatementNode statement5 = (ForEachLoopStatementNode) node;
                return hasReturnValue(statement5.body);

            case WHILE_LOOP_STATEMENT:
                WhileLoopStatementNode statement6 = (WhileLoopStatementNode) node;
                return hasReturnValue(statement6.body);

            default:
                return Optional.empty();
        }
    }
}