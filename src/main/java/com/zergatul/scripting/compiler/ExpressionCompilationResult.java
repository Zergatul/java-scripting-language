package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.runtime.ExpressionEvaluator;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ExpressionCompilationResult {

    private final @Nullable List<DiagnosticMessage> diagnostics;
    private final @Nullable ExpressionEvaluator program;

    private ExpressionCompilationResult(@Nullable List<DiagnosticMessage> diagnostics, @Nullable ExpressionEvaluator program) {
        this.diagnostics = diagnostics;
        this.program = program;
    }

    public static ExpressionCompilationResult failed(List<DiagnosticMessage> diagnostics) {
        return new ExpressionCompilationResult(diagnostics, null);
    }

    public static ExpressionCompilationResult success(ExpressionEvaluator program) {
        return new ExpressionCompilationResult(null, program);
    }

    public boolean isSuccessful() {
        return program != null;
    }

    public List<DiagnosticMessage> getDiagnostics() {
        return Objects.requireNonNull(diagnostics);
    }

    public ExpressionEvaluator getProgram() {
        return Objects.requireNonNull(program);
    }
}