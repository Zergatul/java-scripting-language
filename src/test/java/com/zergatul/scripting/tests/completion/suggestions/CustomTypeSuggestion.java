package com.zergatul.scripting.tests.completion.suggestions;

public class CustomTypeSuggestion extends Suggestion {

    private final Class<?> clazz;

    public CustomTypeSuggestion(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomTypeSuggestion other) {
            return other.clazz == clazz;
        } else {
            return false;
        }
    }
}