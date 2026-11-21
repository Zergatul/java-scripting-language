package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class InstanceCompilationResult {

    private final @Nullable List<DiagnosticMessage> diagnostics;
    private final @Nullable Object program;

    private InstanceCompilationResult(@Nullable List<DiagnosticMessage> diagnostics, @Nullable Object program) {
        this.diagnostics = diagnostics;
        this.program = program;
    }

    public static InstanceCompilationResult failed(List<DiagnosticMessage> diagnostics) {
        return new InstanceCompilationResult(diagnostics, null);
    }

    public static InstanceCompilationResult success(Object program) {
        return new InstanceCompilationResult(null, program);
    }

    public List<DiagnosticMessage> getDiagnostics() {
        if (diagnostics == null) {
            throw new IllegalStateException();
        }
        return diagnostics;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProgram() {
        if (program == null) {
            throw new IllegalStateException();
        }
        return (T) program;
    }

    public boolean isSuccess() {
        return program != null;
    }
}