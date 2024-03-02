package com.zergatul.scripting.parser;

public class Precedences {

    public static int get(UnaryOperator operator) {
        return switch (operator) {
            case PLUS, MINUS, NOT -> 60;
        };
    }

    public static int get(BinaryOperator operator) {
        return switch (operator) {
            case MULTIPLY, DIVIDE, MODULO -> 50;
            case PLUS, MINUS -> 40;
            case AND, OR -> 30;
        };
    }

    public static int getConditionalExpression() {
        return 10;
    }
}