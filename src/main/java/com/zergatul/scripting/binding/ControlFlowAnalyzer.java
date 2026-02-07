package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.*;

import java.util.List;

public class ControlFlowAnalyzer {

    public FlowResult analyzeBlock(BoundBlockStatementNode block) {
        return analyzeStatements(block.statements);
    }

    public FlowResult analyzeStatements(List<BoundStatementNode> statements) {
        for (BoundStatementNode statement : statements) {
            if (analyzeStatement(statement) == FlowResult.TERMINATES) {
                return FlowResult.TERMINATES;
            }
        }
        return FlowResult.CONTINUES;
    }

    public FlowResult analyzeStatement(BoundStatementNode statement) {
        return switch (statement.getNodeType()) {

            case RETURN_STATEMENT, THROW_STATEMENT -> FlowResult.TERMINATES;

            case BREAK_STATEMENT -> {
                BoundBreakStatementNode breakStatement = (BoundBreakStatementNode) statement;
                yield breakStatement.isInsideLoop ? FlowResult.TERMINATES : FlowResult.CONTINUES;
            }

            case CONTINUE_STATEMENT -> {
                BoundContinueStatementNode continueStatement = (BoundContinueStatementNode) statement;
                yield continueStatement.isInsideLoop ? FlowResult.TERMINATES : FlowResult.CONTINUES;
            }

            case BLOCK_STATEMENT -> analyzeBlock((BoundBlockStatementNode) statement);

            case IF_STATEMENT -> {
                BoundIfStatementNode ifStatement = (BoundIfStatementNode) statement;
                FlowResult thenResult = analyzeStatement(ifStatement.thenStatement);
                FlowResult elseResult = ifStatement.elseStatement != null ? analyzeStatement(ifStatement.elseStatement) : FlowResult.CONTINUES;
                yield (thenResult == FlowResult.TERMINATES && elseResult == FlowResult.TERMINATES) ? FlowResult.TERMINATES : FlowResult.CONTINUES;
            }

            case TRY_STATEMENT -> {
                BoundTryStatementNode tryStatement = (BoundTryStatementNode) statement;
                FlowResult finallyBlockResult = tryStatement.finallyBlock != null ? analyzeStatement(tryStatement.finallyBlock) : FlowResult.CONTINUES;
                if (finallyBlockResult == FlowResult.TERMINATES) {
                    yield FlowResult.TERMINATES;
                }

                FlowResult tryBlockResult = analyzeStatement(tryStatement.block);
                boolean normalCompletionPossible =
                        tryBlockResult == FlowResult.CONTINUES ||
                        (tryStatement.catchBlock != null && analyzeStatement(tryStatement.catchBlock) == FlowResult.CONTINUES);

                yield normalCompletionPossible ? FlowResult.CONTINUES : FlowResult.TERMINATES;
            }

            case EXPRESSION_STATEMENT -> {
                BoundExpressionStatementNode expressionStatement = (BoundExpressionStatementNode) statement;
                yield analyzeExpression(expressionStatement.expression);
            }

            default -> FlowResult.CONTINUES;
        };
    }

    private FlowResult analyzeExpression(BoundExpressionNode expression) {
        return switch (expression.getNodeType()) {

            case THROW_EXPRESSION -> FlowResult.TERMINATES;

            case CONVERSION -> {
                BoundConversionNode conversion = (BoundConversionNode) expression;
                yield analyzeExpression(conversion.expression);
            }

            case UNARY_EXPRESSION -> {
                BoundUnaryExpressionNode unary = (BoundUnaryExpressionNode) expression;
                yield analyzeExpression(unary.operand);
            }

            case BINARY_EXPRESSION -> {
                BoundBinaryExpressionNode binary = (BoundBinaryExpressionNode) expression;
                if (analyzeExpression(binary.left) == FlowResult.TERMINATES && analyzeExpression(binary.right) == FlowResult.TERMINATES) {
                    yield FlowResult.TERMINATES;
                } else {
                    yield FlowResult.CONTINUES;
                }
            }

            case CONDITIONAL_EXPRESSION -> {
                BoundConditionalExpressionNode conditionalExpression = (BoundConditionalExpressionNode) expression;
                if (analyzeExpression(conditionalExpression.condition) == FlowResult.TERMINATES) {
                    yield FlowResult.TERMINATES;
                }
                if (analyzeExpression(conditionalExpression.whenTrue) == FlowResult.TERMINATES && analyzeExpression(conditionalExpression.whenFalse) == FlowResult.TERMINATES) {
                    yield FlowResult.TERMINATES;
                }
                yield FlowResult.CONTINUES;
            }

            case BASE_METHOD_INVOCATION_EXPRESSION -> {
                BoundBaseMethodInvocationExpressionNode invocation = (BoundBaseMethodInvocationExpressionNode) expression;
                yield analyzeArguments(invocation.arguments);
            }

            case FUNCTION_INVOCATION -> {
                BoundFunctionInvocationExpression invocation = (BoundFunctionInvocationExpression) expression;
                yield analyzeArguments(invocation.arguments);
            }

            case OBJECT_INVOCATION -> {
                BoundObjectInvocationExpression invocation = (BoundObjectInvocationExpression) expression;
                yield analyzeArguments(invocation.arguments);
            }

            case METHOD_INVOCATION_EXPRESSION -> {
                BoundMethodInvocationExpressionNode invocation = (BoundMethodInvocationExpressionNode) expression;
                if (analyzeExpression(invocation.objectReference) == FlowResult.TERMINATES) {
                    yield FlowResult.TERMINATES;
                }
                yield analyzeArguments(invocation.arguments);
            }

            default -> FlowResult.CONTINUES;
        };
    }

    private FlowResult analyzeArguments(BoundArgumentsListNode list) {
        for (BoundExpressionNode expression : list.arguments) {
            if (analyzeExpression(expression) == FlowResult.TERMINATES) {
                return FlowResult.TERMINATES;
            }
        }

        return FlowResult.CONTINUES;
    }
}