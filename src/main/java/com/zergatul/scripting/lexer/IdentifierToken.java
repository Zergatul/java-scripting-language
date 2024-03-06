package com.zergatul.scripting.lexer;

import com.zergatul.scripting.TextRange;

public class IdentifierToken extends Token {

    public final String value;

    public IdentifierToken(String value, TextRange range) {
        super(TokenType.IDENTIFIER, range);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdentifierToken other) {
            return other.value.equals(value) && super.equals(other);
        } else {
            return false;
        }
    }
}