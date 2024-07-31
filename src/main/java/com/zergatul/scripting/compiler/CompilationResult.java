package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public class CompilationResult {

    private final List<DiagnosticMessage> diagnostics;
    private final Object program;

    private CompilationResult(List<DiagnosticMessage> diagnostics, Object program) {
        this.diagnostics = diagnostics;
        this.program = program;
    }

    public static CompilationResult failed(List<DiagnosticMessage> diagnostics) {
        return new CompilationResult(diagnostics, null);
    }

    public static CompilationResult success(Object program) {
        return new CompilationResult(null, program);
    }

    public List<DiagnosticMessage> getDiagnostics() {
        return diagnostics;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProgram() {
        return (T) program;
    }
}