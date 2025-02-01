package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.symbols.StaticVariable;

public class StaticFieldSuggestion extends Suggestion {

    private final StaticVariable variable;

    public StaticFieldSuggestion(StaticVariable variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StaticFieldSuggestion other) {
            return other.variable.equals(variable);
        } else {
            return false;
        }
    }
}