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
        return switch (type) {
            case PLUS -> UnaryOperator.PLUS;
            case MINUS -> UnaryOperator.MINUS;
            case EXCLAMATION -> UnaryOperator.NOT;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return value;
    }
}