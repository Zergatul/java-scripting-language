package com.zergatul.scripting.tests.completion.suggestions;

public class StaticKeywordSuggestion extends Suggestion {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof StaticKeywordSuggestion;
    }
}