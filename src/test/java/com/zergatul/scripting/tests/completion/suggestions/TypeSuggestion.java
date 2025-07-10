package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.type.SType;

public class TypeSuggestion extends Suggestion {

    private final SType type;

    public TypeSuggestion(SType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeSuggestion other) {
            return other.type.equals(type);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Type[%s]", type);
    }
}