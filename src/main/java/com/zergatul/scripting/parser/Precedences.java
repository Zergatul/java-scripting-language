package com.zergatul.scripting.parser;

import com.zergatul.scripting.InternalException;

public class Precedences {

    public static int getAwait() {
        return 200;
    }

    public static int getThrow() {
        return 80;
    }

    public static int get(UnaryOperator operator) {
        switch (operator) {
            case PLUS:
            case MINUS:
            case NOT:
                return 200;

            default:
                throw new InternalException();
        }
    }

    public static int get(BinaryOperator operator) {
        switch (operator) {
            case MULTIPLY:
            case DIVIDE:
            case MODULO:
                return 180;

            case PLUS:
            case MINUS:
                return 170;

            case LESS:
            case GREATER:
            case LESS_EQUALS:
            case GREATER_EQUALS:
            case IS:
            case AS:
            case IN:
                return 160;

            case EQUALS:
            case NOT_EQUALS:
                return 150;

            case BITWISE_AND:
                return 140;

            case BITWISE_OR:
                return 130;

            case BOOLEAN_AND:
                return 120;

            case BOOLEAN_OR:
                return 110;

            case NULL_COALESCING:
                return 100;

            default:
                throw new InternalException();
        }
    }

    public static int getConditionalExpression() {
        return 90;
    }
}