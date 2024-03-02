package com.zergatul.scripting;

import com.zergatul.scripting.lexer.Token;

public class DiagnosticMessage {

    public DiagnosticLevel level;
    public final String code;
    public final String message;
    public final Token token;

    public DiagnosticMessage(ErrorCode error, Token token, Object... parameters) {
        this.level = DiagnosticLevel.ERROR;
        this.code = error.code();
        this.message = String.format(error.message(), parameters);
        this.token = token;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagnosticMessage other) {
            return  other.level == level &&
                    other.code.equals(code) &&
                    other.message.equals(message) &&
                    other.token.equals(token);
        } else {
            return false;
        }
    }
}