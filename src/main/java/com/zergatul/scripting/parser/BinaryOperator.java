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
        return switch (type) {
            case PLUS -> BinaryOperator.PLUS;
            case MINUS -> BinaryOperator.MINUS;
            case ASTERISK -> BinaryOperator.MULTIPLY;
            case SLASH -> BinaryOperator.DIVIDE;
            case PERCENT -> BinaryOperator.MODULO;
            case EQUAL_EQUAL -> BinaryOperator.EQUALS;
            case EXCLAMATION_EQUAL -> BinaryOperator.NOT_EQUALS;
            case AMPERSAND -> BinaryOperator.BITWISE_AND;
            case AMPERSAND_AMPERSAND -> BinaryOperator.BOOLEAN_AND;
            case PIPE -> BinaryOperator.BITWISE_OR;
            case PIPE_PIPE -> BinaryOperator.BOOLEAN_OR;
            case LESS -> BinaryOperator.LESS;
            case GREATER -> BinaryOperator.GREATER;
            case LESS_EQUAL -> BinaryOperator.LESS_EQUALS;
            case GREATER_EQUAL -> BinaryOperator.GREATER_EQUALS;
            case QUESTION_QUESTION -> BinaryOperator.NULL_COALESCING;
            case IS -> BinaryOperator.IS;
            case AS -> BinaryOperator.AS;
            case IN -> BinaryOperator.IN;
            default -> null;
        };
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