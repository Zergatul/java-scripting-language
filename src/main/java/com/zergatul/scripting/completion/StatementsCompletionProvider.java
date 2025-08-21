package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class StatementsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public StatementsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canStatement()) {
            List<T> suggestions = new ArrayList<>();
            suggestions.add(factory.getKeywordSuggestion(TokenType.LET));
            suggestions.add(factory.getKeywordSuggestion(TokenType.FOR));
            suggestions.add(factory.getKeywordSuggestion(TokenType.FOREACH));
            suggestions.add(factory.getKeywordSuggestion(TokenType.IF));
            suggestions.add(factory.getKeywordSuggestion(TokenType.WHILE));
            suggestions.add(factory.getKeywordSuggestion(TokenType.RETURN));
            return suggestions;
        } else {
            return List.of();
        }
    }
}