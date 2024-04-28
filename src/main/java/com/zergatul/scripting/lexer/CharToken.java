package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class CharToken extends Token {

    public final String value;

    public CharToken(String value, TextRange range) {
        super(TokenType.CHAR_LITERAL, range);
        this.value = value;
    }
}