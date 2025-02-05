package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.DeclaredStaticVariable;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.symbols.StaticVariable;
import com.zergatul.scripting.symbols.Symbol;
import com.zergatul.scripting.tests.completion.TestCompletionContext;
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
        for (Symbol symbol : parameters.getContext().getStaticSymbols()) {
            if (symbol.getName().equals(name)) {
                return (StaticFieldConstantStaticVariable) symbol;
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