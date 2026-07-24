package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.completion.ClassSuggestion;

public class JavaTypeNameSuggestion extends Suggestion {

    private final ClassSuggestion suggestion;

    public JavaTypeNameSuggestion(ClassSuggestion suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JavaTypeNameSuggestion other &&
                other.suggestion.equals(suggestion);
    }

    @Override
    public String toString() {
        return String.format("JavaTypeName[%s, %s]", suggestion.value(), suggestion.type());
    }
}