package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;

import java.util.List;

public class LoopStatementsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public LoopStatementsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canStatement()) {
            return List.of();
        }

        while (context != null && context.entry != null) {
            switch (context.entry.node.getNodeType()) {
                case FOR_LOOP_STATEMENT, FOREACH_LOOP_STATEMENT, WHILE_LOOP_STATEMENT -> {
                    return List.of(factory.getKeywordSuggestion(TokenType.BREAK), factory.getKeywordSuggestion(TokenType.CONTINUE));
                }
                case LAMBDA_EXPRESSION -> {
                    return List.of();
                }
            }
            context = context.up();
        }

        return List.of();
    }
}