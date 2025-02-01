package com.zergatul.scripting.tests.completion.suggestions;

public class VoidKeywordSuggestion extends Suggestion {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidKeywordSuggestion;
    }
}