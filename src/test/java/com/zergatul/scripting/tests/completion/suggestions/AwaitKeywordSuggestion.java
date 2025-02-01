package com.zergatul.scripting.tests.completion.suggestions;

public class AwaitKeywordSuggestion extends Suggestion {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AwaitKeywordSuggestion;
    }
}