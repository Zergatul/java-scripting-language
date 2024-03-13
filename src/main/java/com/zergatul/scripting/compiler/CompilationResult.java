package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public record CompilationResult(List<DiagnosticMessage> diagnostics, Runnable program) {

    public CompilationResult(List<DiagnosticMessage> diagnostics) {
        this(diagnostics, null);
    }

    public CompilationResult(Runnable program) {
        this(null, program);
    }
}