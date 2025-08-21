package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;

import java.util.List;

public abstract class AbstractCompletionProvider<T> {

    protected final SuggestionFactory<T> factory;

    protected AbstractCompletionProvider(SuggestionFactory<T> factory) {
        this.factory = factory;
    }

    public abstract List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context);
}