package com.zergatul.scripting.parser;

import com.zergatul.scripting.lexer.TokenType;
import org.jspecify.annotations.Nullable;

public enum UnaryOperator {
    PLUS("+"),
    MINUS("-"),
    NOT("!");

    private final String value;

    UnaryOperator(String value) {
        this.value = value;
    }

    public static @Nullable UnaryOperator fromToken(TokenType type) {
        switch (type) {
            case PLUS:
                return UnaryOperator.PLUS;
            case MINUS:
                return UnaryOperator.MINUS;
            case EXCLAMATION:
                return UnaryOperator.NOT;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}