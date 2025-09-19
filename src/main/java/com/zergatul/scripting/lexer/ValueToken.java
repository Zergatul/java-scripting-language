package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class ValueToken extends Token {

    public final String value;

    public ValueToken(TokenType tokenType, String value, TextRange range) {
        this(tokenType, value, EMPTY_TRIVIA, EMPTY_TRIVIA, range);
    }

    private ValueToken(TokenType tokenType, String value, Trivia[] leading, Trivia[] trailing, TextRange range) {
        super(tokenType, leading, trailing, range);
        this.value = value;
    }

    @Override
    public Token withTrailingTrivia(Trivia trivia) {
        return new ValueToken(getTokenType(), value, leadingTrivia, merge(trailingTrivia, trivia), getRange());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ValueToken other) {
            return other.value.equals(value) && super.equals(other);
        } else {
            return false;
        }
    }
}