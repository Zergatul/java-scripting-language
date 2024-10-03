package com.zergatul.scripting;

public class DiagnosticMessage {

    public DiagnosticLevel level;
    public final String code;
    public final String message;
    public final TextRange range;

    public DiagnosticMessage(ErrorCode error, Locatable locatable, Object... parameters) {
        this(error, locatable.getRange(), parameters);
    }

    public DiagnosticMessage(ErrorCode error, TextRange range, Object... parameters) {
        this.level = DiagnosticLevel.ERROR;
        this.code = error.code();
        this.message = String.format(error.message(), parameters);
        this.range = range;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagnosticMessage other) {
            return  other.level == level &&
                    other.code.equals(code) &&
                    other.message.equals(message) &&
                    other.range.equals(range);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return message;
    }
}