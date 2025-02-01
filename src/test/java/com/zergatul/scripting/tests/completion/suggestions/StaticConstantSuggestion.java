package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.symbols.StaticVariable;

public class StaticConstantSuggestion extends Suggestion {

    private final StaticVariable variable;

    public StaticConstantSuggestion(StaticVariable variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StaticConstantSuggestion other) {
            return other.variable.equals(variable);
        } else {
            return false;
        }
    }
}