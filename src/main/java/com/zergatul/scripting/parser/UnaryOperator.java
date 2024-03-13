package com.zergatul.scripting.parser;

public enum UnaryOperator {
    PLUS("+"),
    MINUS("-"),
    NOT("!");

    private final String value;

    UnaryOperator(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}