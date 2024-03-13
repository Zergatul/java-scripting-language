package com.zergatul.scripting.parser;

public enum BinaryOperator {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    AND("&&"),
    OR("||");

    private final String value;

    BinaryOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}