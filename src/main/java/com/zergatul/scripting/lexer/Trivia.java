package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class Trivia extends Token {
    public Trivia(TokenType type, TextRange range) {
        super(type, range);
    }
}