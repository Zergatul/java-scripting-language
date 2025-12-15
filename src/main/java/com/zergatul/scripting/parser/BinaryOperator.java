package com.zergatul.scripting.parser;

public enum BinaryOperator {
    PLUS("+", true),
    MINUS("-", true),
    MULTIPLY("*", true),
    DIVIDE("/", true),
    MODULO("%", true),
    BOOLEAN_AND("&&", false),
    BOOLEAN_OR("||", false),
    EQUALS("==", true),
    NOT_EQUALS("!=", true),
    LESS("<", true),
    GREATER(">", true),
    LESS_EQUALS("<=", true),
    GREATER_EQUALS(">=", true),
    BITWISE_AND("&", true),
    BITWISE_OR("|", true),
    IS("is", false),
    AS("as", false),
    IN("in", false);

    private final String value;
    private final boolean canBeOverloaded;

    BinaryOperator(String value, boolean canBeOverloaded) {
        this.value = value;
        this.canBeOverloaded = canBeOverloaded;
    }

    public boolean canBeOverloaded() {
        return canBeOverloaded;
    }

    @Override
    public String toString() {
        return value;
    }
}