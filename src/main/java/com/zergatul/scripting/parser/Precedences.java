package com.zergatul.scripting.parser;

public class Precedences {

    public static int getAwait() {
        return 100;
    }

    public static int get(UnaryOperator operator) {
        return switch (operator) {
            case PLUS, MINUS, NOT -> 100;
        };
    }

    public static int get(BinaryOperator operator) {
        return switch (operator) {
            case MULTIPLY, DIVIDE, MODULO -> 80;
            case PLUS, MINUS -> 70;
            case LESS, GREATER, LESS_EQUALS, GREATER_EQUALS, IS -> 60;
            case EQUALS, NOT_EQUALS -> 50;
            case BITWISE_AND -> 40;
            case BITWISE_OR -> 30;
            case BOOLEAN_AND -> 20;
            case BOOLEAN_OR -> 10;
        };
    }

    public static int getConditionalExpression() {
        return 5;
    }
}