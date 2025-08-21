package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.ArrayList;
import java.util.List;

public class StaticConstantsCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public StaticConstantsCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression()) {
            List<T> suggestions = new ArrayList<>();
            for (SymbolRef ref : output.context().getStaticSymbols()) {
                if (ref.get() instanceof StaticFieldConstantStaticVariable constant) {
                    suggestions.add(factory.getStaticConstantSuggestion(constant));
                }
            }
            return suggestions;
        } else {
            return List.of();
        }
    }
}