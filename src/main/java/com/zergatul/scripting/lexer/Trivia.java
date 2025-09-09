package com.zergatul.scripting.lexer;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;

public class Trivia extends Token {

    public Trivia(TokenType type, TextRange range) {
        super(type, range);
    }

    @Override
    public String asFullSource(String code) {
        throw new InternalException();
    }
}