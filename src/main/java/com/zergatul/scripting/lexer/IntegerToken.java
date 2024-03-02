package com.zergatul.scripting.lexer;

public class IntegerToken extends Token {

    public final String value;

    public IntegerToken(String value, int line, int column, int position, int length) {
        super(TokenType.INTEGER_LITERAL, line, column, position, length);
        this.value = value;
    }
}