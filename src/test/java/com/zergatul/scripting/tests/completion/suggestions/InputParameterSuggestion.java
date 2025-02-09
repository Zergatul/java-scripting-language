package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.type.SType;

public class InputParameterSuggestion extends Suggestion {

    private final String name;
    private final SType type;

    public InputParameterSuggestion(String name, SType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InputParameterSuggestion other) {
            return other.name.equals(name) && other.type.equals(type);
        } else {
            return false;
        }
    }
}