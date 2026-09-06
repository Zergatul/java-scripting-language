package com.zergatul.scripting.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class LexerOutput {

    private final String code;
    private final List<Line> lines;
    private final TokenQueue tokens;
    private final List<DiagnosticMessage> diagnostics;

    public LexerOutput(String code, List<Line> lines, TokenQueue tokens, List<DiagnosticMessage> diagnostics) {
        this.code = code;
        this.lines = lines;
        this.tokens = tokens;
        this.diagnostics = diagnostics;
    }

    public String code() {
        return code;
    }

    public List<Line> lines() {
        return lines;
    }

    public TokenQueue tokens() {
        return tokens;
    }

    public List<DiagnosticMessage> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        LexerOutput that = (LexerOutput) obj;
        return  Objects.equals(this.code, that.code) &&
                Objects.equals(this.lines, that.lines) &&
                Objects.equals(this.tokens, that.tokens) &&
                Objects.equals(this.diagnostics, that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, lines, tokens, diagnostics);
    }

    @Override
    public String toString() {
        return  "LexerOutput[" +
                "code=" + code + ", " +
                "lines=" + lines + ", " +
                "tokens=" + tokens + ", " +
                "diagnostics=" + diagnostics + ']';
    }
}