package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class VoidCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public VoidCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canVoid()) {
            return Lists.of(factory.getKeywordSuggestion(TokenType.VOID));
        } else {
            return Lists.of();
        }
    }
}