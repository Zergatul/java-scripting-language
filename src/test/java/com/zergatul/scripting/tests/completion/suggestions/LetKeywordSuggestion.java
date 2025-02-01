package com.zergatul.scripting.tests.completion.suggestions;

public class LetKeywordSuggestion extends Suggestion {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LetKeywordSuggestion;
    }
}