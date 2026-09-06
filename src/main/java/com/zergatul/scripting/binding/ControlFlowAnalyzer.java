package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.*;

import java.util.List;

import static com.zergatul.scripting.binding.FlowResult.CONTINUES;
import static com.zergatul.scripting.binding.FlowResult.TERMINATES;
import static java.lang.Thread.yield;

public class ControlFlowAnalyzer {

    public FlowResult analyzeBlock(BoundBlockStatementNode block) {
        return analyzeStatements(block.statements);
    }

    public FlowResult analyzeStatements(List<BoundStatementNode> statements) {
        for (BoundStatementNode statement : statements) {
            if (analyzeStatement(statement) == TERMINATES) {
                return TERMINATES;
            }
        }
        return CONTINUES;
    }

    public FlowResult analyzeStatement(BoundStatementNode statement) {
        switch (statement.getNodeType()) {
            case RETURN_STATEMENT:
            case THROW_STATEMENT:
                return TERMINATES;

            case BREAK_STATEMENT:
                BoundBreakStatementNode breakStatement = (BoundBreakStatementNode) statement;
                return breakStatement.isInsideLoop ? TERMINATES : CONTINUES;

            case CONTINUE_STATEMENT:
                BoundContinueStatementNode continueStatement = (BoundContinueStatementNode) statement;
                return continueStatement.isInsideLoop ? TERMINATES : CONTINUES;

            case BLOCK_STATEMENT:
                return analyzeBlock((BoundBlockStatementNode) statement);

            case IF_STATEMENT:
                BoundIfStatementNode ifStatement = (BoundIfStatementNode) statement;
                FlowResult thenResult = analyzeStatement(ifStatement.thenStatement);
                FlowResult elseResult = ifStatement.elseStatement != null ? analyzeStatement(ifStatement.elseStatement) : CONTINUES;
                return (thenResult == TERMINATES && elseResult == TERMINATES) ? TERMINATES : CONTINUES;

            case TRY_STATEMENT:
                BoundTryStatementNode tryStatement = (BoundTryStatementNode) statement;
                FlowResult finallyBlockResult = tryStatement.finallyBlock != null ? analyzeStatement(tryStatement.finallyBlock) : CONTINUES;
                if (finallyBlockResult == TERMINATES) {
                    return TERMINATES;
                }

                FlowResult tryBlockResult = analyzeStatement(tryStatement.block);
                boolean normalCompletionPossible =
                        tryBlockResult == CONTINUES ||
                                (tryStatement.catchBlock != null && analyzeStatement(tryStatement.catchBlock) == CONTINUES);
                return normalCompletionPossible ? CONTINUES : TERMINATES;

            case EXPRESSION_STATEMENT:
                BoundExpressionStatementNode expressionStatement = (BoundExpressionStatementNode) statement;
                return analyzeExpression (expressionStatement.expression);

            default:
                return CONTINUES;
        }
    }

    private FlowResult analyzeExpression(BoundExpressionNode expression) {
        switch (expression.getNodeType()) {
            case THROW_EXPRESSION:
                return TERMINATES;

            case CONVERSION:
                BoundConversionNode conversion = (BoundConversionNode) expression;
                return analyzeExpression (conversion.expression);

            case UNARY_EXPRESSION:
                BoundUnaryExpressionNode unary = (BoundUnaryExpressionNode) expression;
                return analyzeExpression (unary.operand);

            case BINARY_EXPRESSION:
                BoundBinaryExpressionNode binary = (BoundBinaryExpressionNode) expression;
                if (analyzeExpression(binary.left) == TERMINATES && analyzeExpression(binary.right) == TERMINATES) {
                    return FlowResult.TERMINATES;
                } else {
                    return FlowResult.CONTINUES;
                }

            case CONDITIONAL_EXPRESSION:
                BoundConditionalExpressionNode conditionalExpression = (BoundConditionalExpressionNode) expression;
                if (analyzeExpression(conditionalExpression.condition) == TERMINATES) {
                    return FlowResult.TERMINATES;
                }
                if (analyzeExpression(conditionalExpression.whenTrue) == TERMINATES && analyzeExpression(conditionalExpression.whenFalse) == TERMINATES) {
                    return FlowResult.TERMINATES;
                }
                return FlowResult.CONTINUES;

            case BASE_METHOD_INVOCATION_EXPRESSION:
                BoundBaseMethodInvocationExpressionNode invocation1 = (BoundBaseMethodInvocationExpressionNode) expression;
                return analyzeArguments(invocation1.arguments);

            case FUNCTION_INVOCATION:
                BoundFunctionInvocationExpression invocation2 = (BoundFunctionInvocationExpression) expression;
                return analyzeArguments(invocation2.arguments);

            case OBJECT_INVOCATION:
                BoundObjectInvocationExpression invocation3 = (BoundObjectInvocationExpression) expression;
                return analyzeArguments(invocation3.arguments);

            case METHOD_INVOCATION_EXPRESSION:
                BoundMethodInvocationExpressionNode invocation4 = (BoundMethodInvocationExpressionNode) expression;
                if (analyzeExpression(invocation4.objectReference) == TERMINATES) {
                    return FlowResult.TERMINATES;
                }
                return analyzeArguments(invocation4.arguments);

            default:
                return CONTINUES;
        }
    }

    private FlowResult analyzeArguments(BoundArgumentsListNode list) {
        for (BoundExpressionNode expression : list.arguments) {
            if (analyzeExpression(expression) == TERMINATES) {
                return TERMINATES;
            }
        }

        return CONTINUES;
    }
}