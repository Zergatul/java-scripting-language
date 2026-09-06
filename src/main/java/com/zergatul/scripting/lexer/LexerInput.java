package com.zergatul.scripting.lexer;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class LexerInput {

    private final String code;

    public LexerInput(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        LexerInput that = (LexerInput) obj;
        return Objects.equals(this.code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return  "LexerInput[" +
                "code=" + code + ']';
    }
}