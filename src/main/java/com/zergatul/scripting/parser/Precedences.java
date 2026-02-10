package com.zergatul.scripting.parser;

public class Precedences {

    public static int getAwait() {
        return 200;
    }

    public static int getThrow() {
        return 80;
    }

    public static int get(UnaryOperator operator) {
        return switch (operator) {
            case PLUS, MINUS, NOT -> 200;
        };
    }

    public static int get(BinaryOperator operator) {
        return switch (operator) {
            case MULTIPLY, DIVIDE, MODULO -> 180;
            case PLUS, MINUS -> 170;
            case LESS, GREATER, LESS_EQUALS, GREATER_EQUALS, IS, AS, IN -> 160;
            case EQUALS, NOT_EQUALS -> 150;
            case BITWISE_AND -> 140;
            case BITWISE_OR -> 130;
            case BOOLEAN_AND -> 120;
            case BOOLEAN_OR -> 110;
            case NULL_COALESCING -> 100;
        };
    }

    public static int getConditionalExpression() {
        return 90;
    }
}