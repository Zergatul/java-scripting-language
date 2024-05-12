package com.zergatul.scripting.parser;

public enum PostfixOperator {
    PLUS_PLUS("+"),
    MINUS_MINUS("-");

    private final String value;

    PostfixOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}