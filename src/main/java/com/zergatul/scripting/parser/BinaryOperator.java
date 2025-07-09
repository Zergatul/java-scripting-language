package com.zergatul.scripting.parser;

public enum BinaryOperator {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    BOOLEAN_AND("&&"),
    BOOLEAN_OR("||"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS("<"),
    GREATER(">"),
    LESS_EQUALS("<="),
    GREATER_EQUALS(">="),
    BITWISE_AND("&"),
    BITWISE_OR("|"),
    IS("is"),
    AS("as");

    private final String value;

    BinaryOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}