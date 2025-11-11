package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.type.SAliasType;

public class TypeAliasSuggestion extends Suggestion {

    private final SAliasType type;

    public TypeAliasSuggestion(SAliasType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeAliasSuggestion other) {
            return other.type.equals(type);
        } else {
            return false;
        }
    }
}