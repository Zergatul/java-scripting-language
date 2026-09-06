package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundForEachLoopStatementNode;
import com.zergatul.scripting.binding.nodes.BoundForLoopStatementNode;
import com.zergatul.scripting.binding.nodes.BoundWhileLoopStatementNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class LoopStatementsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public LoopStatementsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canStatement()) {
            return Lists.of();
        }

        while (context != null && context.entry != null) {
            switch (context.entry.node.getNodeType()) {
                case FOR_LOOP_STATEMENT: {
                    BoundForLoopStatementNode loop = (BoundForLoopStatementNode) context.entry.node;
                    if (!loop.syntaxNode.closeParen.isMissing() && loop.syntaxNode.closeParen.getRange().isBefore(context.line, context.column)) {
                        return getKeywords();
                    }
                    break;
                }
                case FOREACH_LOOP_STATEMENT: {
                    BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) context.entry.node;
                    if (!loop.syntaxNode.closeParen.isMissing() && loop.syntaxNode.closeParen.getRange().isBefore(context.line, context.column)) {
                        return getKeywords();
                    }
                    break;
                }
                case WHILE_LOOP_STATEMENT: {
                    BoundWhileLoopStatementNode loop = (BoundWhileLoopStatementNode) context.entry.node;
                    if (!loop.syntaxNode.closeParen.isMissing() && loop.syntaxNode.closeParen.getRange().isBefore(context.line, context.column)) {
                        return getKeywords();
                    }
                    break;
                }
                case LAMBDA_EXPRESSION: {
                    return Lists.of();
                }
            }
            context = context.up();
        }

        return Lists.of();
    }

    private List<T> getKeywords() {
        return Lists.of(factory.getKeywordSuggestion(TokenType.BREAK), factory.getKeywordSuggestion(TokenType.CONTINUE));
    }
}