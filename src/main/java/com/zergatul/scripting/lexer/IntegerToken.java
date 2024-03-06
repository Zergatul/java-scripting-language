package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class IntegerToken extends Token {

    public final String value;

    public IntegerToken(String value, TextRange range) {
        super(TokenType.INTEGER_LITERAL, range);
        this.value = value;
    }
}