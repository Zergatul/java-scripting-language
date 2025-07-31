package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class CharToken extends Token {

    public final char value;

    public CharToken(char value, TextRange range) {
        super(TokenType.CHAR_LITERAL, range);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CharToken other) {
            return other.value == value && super.equals(other);
        } else {
            return false;
        }
    }
}