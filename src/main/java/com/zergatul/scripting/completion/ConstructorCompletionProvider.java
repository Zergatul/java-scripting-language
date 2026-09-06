package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class ConstructorCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ConstructorCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canClassMember()) {
            return Lists.of(factory.getKeywordSuggestion(TokenType.CONSTRUCTOR));
        } else {
            return Lists.of();
        }
    }
}