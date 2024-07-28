package com.zergatul.scripting.compiler;

import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public record CompilationResult<T>(List<DiagnosticMessage> diagnostics, T program) {

    public CompilationResult(List<DiagnosticMessage> diagnostics) {
        this(diagnostics, null);
    }

    public CompilationResult(T program) {
        this(null, program);
    }
}