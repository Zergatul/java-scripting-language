package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.symbols.LocalVariable;

public class LocalVariableSuggestion extends Suggestion {

    private final LocalVariable variable;

    public LocalVariableSuggestion(LocalVariable variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalVariableSuggestion other) {
            return other.variable.equals(variable);
        } else {
            return false;
        }
    }
}