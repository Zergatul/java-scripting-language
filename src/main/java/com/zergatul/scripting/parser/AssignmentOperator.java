package com.zergatul.scripting.parser;

public enum AssignmentOperator {
    ASSIGNMENT(null),
    PLUS_ASSIGNMENT(BinaryOperator.PLUS),
    MINUS_ASSIGNMENT(BinaryOperator.MINUS),
    MULTIPLY_ASSIGNMENT(BinaryOperator.MULTIPLY),
    DIVIDE_ASSIGNMENT(BinaryOperator.DIVIDE),
    MODULO_ASSIGNMENT(BinaryOperator.MODULO),
    AND_ASSIGNMENT(BinaryOperator.BITWISE_AND),
    OR_ASSIGNMENT(BinaryOperator.BITWISE_OR);

    private final BinaryOperator binary;

    AssignmentOperator(BinaryOperator binary) {
        this.binary = binary;
    }

    public BinaryOperator getBinaryOperator() {
        return binary;
    }
}