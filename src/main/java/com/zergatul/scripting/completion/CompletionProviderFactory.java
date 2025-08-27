package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitNode;
import com.zergatul.scripting.compiler.CompilationParameters;

import java.util.ArrayList;
import java.util.List;

public class CompletionProviderFactory<T> {

    private final List<AbstractCompletionProvider<T>> providers;

    public CompletionProviderFactory(SuggestionFactory<T> factory) {
        providers = List.of(
                new StaticConstantsCompletionProvider<>(factory),
                new TypesCompletionProvider<>(factory),
                new MetaExpressionsCompletionProvider<>(factory),
                new LocalVariablesCompletionProvider<>(factory),
                new ObjectMemberCompletionProvider<>(factory),
                new StatementsCompletionProvider<>(factory),
                new ElseKeywordCompletionProvider<>(factory),
                new LoopStatementsCompletionProvider<>(factory),
                new LoopVariablesCompletionProvider<>(factory));
    }

    public List<T> getSuggestions(CompilationParameters parameters, BinderOutput output, int line, int column) {
        BoundCompilationUnitNode unit = output.unit();
        CompletionContext completionContext = CompletionContext.create(unit, line, column);
        List<T> suggestions = new ArrayList<>();
        for (var provider : providers) {
            suggestions.addAll(provider.provide(parameters, output, completionContext));
        }
        return suggestions;
    }
}