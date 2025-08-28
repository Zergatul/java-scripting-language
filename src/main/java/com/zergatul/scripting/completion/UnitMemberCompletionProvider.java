package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;

import java.util.List;

public class UnitMemberCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public UnitMemberCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canUnitMember()) {
            return List.of(
                    factory.getKeywordSuggestion(TokenType.STATIC),
                    factory.getKeywordSuggestion(TokenType.CLASS));
        } else {
            return List.of();
        }
    }
}