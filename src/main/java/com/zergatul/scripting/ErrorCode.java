package com.zergatul.scripting;

public record ErrorCode(String code, String message, DiagnosticLevel level) {
    public ErrorCode(String code, String message) {
        this(code, message, DiagnosticLevel.ERROR);
    }
}