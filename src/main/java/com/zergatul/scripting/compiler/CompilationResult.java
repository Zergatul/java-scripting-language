package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CompilationResult {

    private final @Nullable List<DiagnosticMessage> diagnostics;
    private final @Nullable Object program;

    private CompilationResult(@Nullable List<DiagnosticMessage> diagnostics, @Nullable Object program) {
        this.diagnostics = diagnostics;
        this.program = program;
    }

    public static CompilationResult failed(List<DiagnosticMessage> diagnostics) {
        return new CompilationResult(diagnostics, null);
    }

    public static CompilationResult success(Object program) {
        return new CompilationResult(null, program);
    }

    public @Nullable List<DiagnosticMessage> getDiagnostics() {
        return diagnostics;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getProgram() {
        return (T) program;
    }
}