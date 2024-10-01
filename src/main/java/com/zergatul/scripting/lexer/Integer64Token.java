package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class Integer64Token extends Token {

    public final String value;

    public Integer64Token(String value, TextRange range) {
        super(TokenType.INTEGER64_LITERAL, range);
        this.value = value;
    }
}