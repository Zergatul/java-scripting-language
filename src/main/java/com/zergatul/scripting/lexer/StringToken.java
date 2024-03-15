package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class StringToken extends Token {

    public final String value;

    public StringToken(String value, TextRange range) {
        super(TokenType.STRING_LITERAL, range);
        this.value = value;
    }
}