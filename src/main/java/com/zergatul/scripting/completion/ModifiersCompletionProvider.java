package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;

import java.util.List;

public class ModifiersCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ModifiersCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canClassMember()) {
            return List.of(
                    factory.getKeywordSuggestion(TokenType.ASYNC),
                    factory.getKeywordSuggestion(TokenType.VIRTUAL),
                    factory.getKeywordSuggestion(TokenType.OVERRIDE));
        } else {
            return List.of();
        }
    }
}