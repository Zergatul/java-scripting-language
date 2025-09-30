package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;

import java.util.List;

public class BasicExpressionStartCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public BasicExpressionStartCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression()) {
            return List.of(
                    factory.getKeywordSuggestion(TokenType.FALSE),
                    factory.getKeywordSuggestion(TokenType.TRUE),
                    factory.getKeywordSuggestion(TokenType.NEW));
        } else {
            return List.of();
        }
    }
}