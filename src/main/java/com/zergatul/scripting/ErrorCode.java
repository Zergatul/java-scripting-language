package com.zergatul.scripting;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ErrorCode {

    private final String code;
    private final String message;
    private final DiagnosticLevel level;

    public ErrorCode(String code, String message, DiagnosticLevel level) {
        this.code = code;
        this.message = message;
        this.level = level;
    }

    public ErrorCode(String code, String message) {
        this(code, message, DiagnosticLevel.ERROR);
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public DiagnosticLevel level() {
        return level;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ErrorCode that = (ErrorCode) obj;
        return  Objects.equals(this.code, that.code) &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.level, that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, level);
    }

    @Override
    public String toString() {
        return  "ErrorCode[" +
                "code=" + code + ", " +
                "message=" + message + ", " +
                "level=" + level + ']';
    }
}