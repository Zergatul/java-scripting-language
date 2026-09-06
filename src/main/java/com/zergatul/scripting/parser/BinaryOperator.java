package com.zergatul.scripting.parser;

import com.zergatul.scripting.lexer.TokenType;
import org.jspecify.annotations.Nullable;

public enum BinaryOperator {
    PLUS(builder("+")),
    MINUS(builder("-")),
    MULTIPLY(builder("*")),
    DIVIDE(builder("/")),
    MODULO(builder("%")),
    BOOLEAN_AND(builder("&&").notOverloadable()),
    BOOLEAN_OR(builder("||").notOverloadable()),
    EQUALS(builder("==").booleanOnly()),
    NOT_EQUALS(builder("!=").booleanOnly()),
    LESS(builder("<").booleanOnly()),
    GREATER(builder(">").booleanOnly()),
    LESS_EQUALS(builder("<=").booleanOnly()),
    GREATER_EQUALS(builder(">=").booleanOnly()),
    BITWISE_AND(builder("&")),
    BITWISE_OR(builder("|")),
    NULL_COALESCING(builder("??").notOverloadable().allowThrowRightHandSide().rightAssociative()),
    IS(builder("is").notOverloadable()),
    AS(builder("as").notOverloadable()),
    IN(builder("in").notOverloadable());

    private final String value;
    private final boolean canBeOverloaded;
    private final boolean allowThrowRight;
    private final boolean booleanOnlyResult;
    private final boolean isRightAssociative;

    BinaryOperator(Builder builder) {
        this.value = builder.value;
        this.canBeOverloaded = builder.canBeOverloaded;
        this.allowThrowRight = builder.allowThrowRight;
        this.booleanOnlyResult = builder.booleanOnlyResult;
        this.isRightAssociative = builder.isRightAssociative;
    }

    public static @Nullable BinaryOperator fromToken(TokenType type) {
        switch (type) {
            case PLUS:
                return BinaryOperator.PLUS;
            case MINUS:
                return BinaryOperator.MINUS;
            case ASTERISK:
                return BinaryOperator.MULTIPLY;
            case SLASH:
                return BinaryOperator.DIVIDE;
            case PERCENT:
                return BinaryOperator.MODULO;
            case EQUAL_EQUAL:
                return BinaryOperator.EQUALS;
            case EXCLAMATION_EQUAL:
                return BinaryOperator.NOT_EQUALS;
            case AMPERSAND:
                return BinaryOperator.BITWISE_AND;
            case AMPERSAND_AMPERSAND:
                return BinaryOperator.BOOLEAN_AND;
            case PIPE:
                return BinaryOperator.BITWISE_OR;
            case PIPE_PIPE:
                return BinaryOperator.BOOLEAN_OR;
            case LESS:
                return BinaryOperator.LESS;
            case GREATER:
                return BinaryOperator.GREATER;
            case LESS_EQUAL:
                return BinaryOperator.LESS_EQUALS;
            case GREATER_EQUAL:
                return BinaryOperator.GREATER_EQUALS;
            case QUESTION_QUESTION:
                return BinaryOperator.NULL_COALESCING;
            case IS:
                return BinaryOperator.IS;
            case AS:
                return BinaryOperator.AS;
            case IN:
                return BinaryOperator.IN;
            default:
                return null;
        }
    }

    public boolean canBeOverloaded() {
        return canBeOverloaded;
    }

    public boolean isBooleanOnlyResult() {
        return booleanOnlyResult;
    }

    public boolean isThrowOnTheRightSideAllowed() {
        return allowThrowRight;
    }

    public boolean isRightAssociative() {
        return isRightAssociative;
    }

    @Override
    public String toString() {
        return value;
    }

    private static Builder builder(String value) {
        return new Builder(value);
    }

    private static final class Builder {

        private final String value;
        private boolean canBeOverloaded = true;
        private boolean allowThrowRight = false;
        private boolean booleanOnlyResult = false;
        private boolean isRightAssociative = false;

        private Builder(String value) {
            this.value = value;
        }

        Builder notOverloadable() {
            this.canBeOverloaded = false;
            return this;
        }

        Builder allowThrowRightHandSide() {
            this.allowThrowRight = true;
            return this;
        }

        Builder booleanOnly() {
            this.booleanOnlyResult = true;
            return this;
        }

        Builder rightAssociative() {
            this.isRightAssociative = true;
            return this;
        }
    }
}