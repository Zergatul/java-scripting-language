package com.zergatul.scripting.parser;

import org.jspecify.annotations.Nullable;

public enum AssignmentOperator {
    ASSIGNMENT(null),
    PLUS_ASSIGNMENT(BinaryOperator.PLUS),
    MINUS_ASSIGNMENT(BinaryOperator.MINUS),
    MULTIPLY_ASSIGNMENT(BinaryOperator.MULTIPLY),
    DIVIDE_ASSIGNMENT(BinaryOperator.DIVIDE),
    MODULO_ASSIGNMENT(BinaryOperator.MODULO),
    AND_ASSIGNMENT(BinaryOperator.BITWISE_AND),
    OR_ASSIGNMENT(BinaryOperator.BITWISE_OR);

    @Nullable
    private final BinaryOperator binary;

    AssignmentOperator(@Nullable BinaryOperator binary) {
        this.binary = binary;
    }

    @Nullable
    public BinaryOperator getBinaryOperator() {
        return binary;
    }
}