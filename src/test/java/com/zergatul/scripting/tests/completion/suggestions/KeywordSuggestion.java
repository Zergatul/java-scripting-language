package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.lexer.TokenType;

public class KeywordSuggestion extends Suggestion {

    private final TokenType type;

    public KeywordSuggestion(TokenType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeywordSuggestion other) {
            return other.type == type;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Keyword[%s]", type);
    }
}