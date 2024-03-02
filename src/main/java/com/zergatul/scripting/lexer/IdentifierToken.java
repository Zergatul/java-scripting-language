package com.zergatul.scripting.lexer;

public class IdentifierToken extends Token {

    public final String value;

    public IdentifierToken(String value, int line, int column, int position, int length) {
        super(TokenType.IDENTIFIER, line, column, position, length);
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