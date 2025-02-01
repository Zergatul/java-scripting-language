package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.type.MethodReference;

public class MethodSuggestion extends Suggestion {

    private final MethodReference method;

    public MethodSuggestion(MethodReference method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodSuggestion other) {
            return other.method.equals(method);
        } else {
            return false;
        }
    }
}