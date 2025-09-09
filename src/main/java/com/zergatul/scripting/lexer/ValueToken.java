package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

import java.util.List;

public final class ValueToken extends Token {

    public final String value;

    public ValueToken(TokenType tokenType, String value, TextRange range) {
        this(tokenType, value, EMPTY_TRIVIA, EMPTY_TRIVIA, range);
    }

    private ValueToken(TokenType tokenType, String value, Trivia[] leading, Trivia[] trailing, TextRange range) {
        super(tokenType, leading, trailing, range);
        this.value = value;
    }

    @Override
    public ValueToken withLeadingTrivia(List<Trivia> trivia) {
        return new ValueToken(getTokenType(), value, merge(leadingTrivia, trivia), trailingTrivia, getRange());
    }

    @Override
    public ValueToken withTrailingTrivia(Trivia trivia) {
        return new ValueToken(getTokenType(), value, leadingTrivia, merge(trailingTrivia, trivia), getRange());
    }
}