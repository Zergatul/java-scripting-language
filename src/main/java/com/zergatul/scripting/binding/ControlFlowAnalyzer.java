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

            case RETURN_STATEMENT -> FlowResult.TERMINATES;

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

            default -> FlowResult.CONTINUES;
        };
    }
}