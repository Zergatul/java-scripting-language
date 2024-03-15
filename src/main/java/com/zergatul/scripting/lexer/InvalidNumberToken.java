package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class InvalidNumberToken extends Token {

    public final String value;

    public InvalidNumberToken(String value, TextRange range) {
        super(TokenType.INVALID_NUMBER, range);
        this.value = value;
    }
}