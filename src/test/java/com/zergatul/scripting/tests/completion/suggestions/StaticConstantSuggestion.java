package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import org.junit.jupiter.api.Assertions;

public class StaticConstantSuggestion extends Suggestion {

    private final StaticFieldConstantStaticVariable variable;

    public StaticConstantSuggestion(TestCompletionContext context, String name) {
        this(extract(context.parameters(), name));
    }

    public StaticConstantSuggestion(StaticFieldConstantStaticVariable variable) {
        this.variable = variable;
    }

    private static StaticFieldConstantStaticVariable extract(CompilationParameters parameters, String name) {
        for (SymbolRef symbolRef : parameters.getContext().getStaticSymbols()) {
            if (symbolRef.get().getName().equals(name)) {
                return (StaticFieldConstantStaticVariable) symbolRef.get();
            }
        }
        Assertions.fail();
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StaticConstantSuggestion other) {
            return other.variable == variable;
        } else {
            return false;
        }
    }
}