package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

import java.util.List;

public class EndOfFileToken extends Token {

    public EndOfFileToken(TextRange range) {
        this(EMPTY_TRIVIA, range);
    }

    private EndOfFileToken(Trivia[] leading, TextRange range) {
        super(TokenType.END_OF_FILE, leading, EMPTY_TRIVIA, range);
    }

    @Override
    public String getRawValue(String code) {
        return "<EOF>";
    }

    @Override
    public Token withLeadingTrivia(List<Trivia> trivia) {
        return new EndOfFileToken(merge(leadingTrivia, trivia), getRange());
    }
}