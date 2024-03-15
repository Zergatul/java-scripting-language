package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class FloatToken extends Token {

    public final String value;

    public FloatToken(String value, TextRange range) {
        super(TokenType.FLOAT_LITERAL, range);
        this.value = value;
    }
}