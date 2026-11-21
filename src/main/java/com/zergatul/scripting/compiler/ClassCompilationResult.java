package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ClassCompilationResult {

    private final @Nullable List<DiagnosticMessage> diagnostics;
    private final @Nullable Class<?> program;

    private ClassCompilationResult(@Nullable List<DiagnosticMessage> diagnostics, @Nullable Class<?> program) {
        this.diagnostics = diagnostics;
        this.program = program;
    }

    public static ClassCompilationResult failed(List<DiagnosticMessage> diagnostics) {
        return new ClassCompilationResult(diagnostics, null);
    }

    public static ClassCompilationResult success(Class<?> program) {
        return new ClassCompilationResult(null, program);
    }

    public List<DiagnosticMessage> getDiagnostics() {
        if (diagnostics == null) {
            throw new IllegalStateException();
        }
        return diagnostics;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> getProgram() {
        if (program == null) {
            throw new IllegalStateException();
        }
        return (Class<T>) program;
    }

    public boolean isSuccess() {
        return program != null;
    }
}