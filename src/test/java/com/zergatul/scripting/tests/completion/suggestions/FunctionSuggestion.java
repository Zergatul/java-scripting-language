package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.symbols.Function;

public class FunctionSuggestion extends Suggestion {

    private final Function function;

    public FunctionSuggestion(Function function) {
        this.function = function;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionSuggestion other) {
            return other.function.equals(function);
        } else {
            return false;
        }
    }
}