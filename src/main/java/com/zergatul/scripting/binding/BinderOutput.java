package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitNode;
import com.zergatul.scripting.compiler.CompilerContext;

import java.util.List;
import java.util.Objects;

public final class BinderOutput {

    private final String code;
    private final BoundCompilationUnitNode unit;
    private final CompilerContext context;
    private final List<DiagnosticMessage> diagnostics;

    public BinderOutput(
            String code,
            BoundCompilationUnitNode unit,
            CompilerContext context,
            List<DiagnosticMessage> diagnostics
    ) {
        this.code = code;
        this.unit = unit;
        this.context = context;
        this.diagnostics = diagnostics;
    }

    public String code() {
        return code;
    }

    public BoundCompilationUnitNode unit() {
        return unit;
    }

    public CompilerContext context() {
        return context;
    }

    public List<DiagnosticMessage> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BinderOutput that = (BinderOutput) obj;
        return  Objects.equals(this.code, that.code) &&
                Objects.equals(this.unit, that.unit) &&
                Objects.equals(this.context, that.context) &&
                Objects.equals(this.diagnostics, that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, unit, context, diagnostics);
    }

    @Override
    public String toString() {
        return  "BinderOutput[" +
                "code=" + code + ", " +
                "unit=" + unit + ", " +
                "context=" + context + ", " +
                "diagnostics=" + diagnostics + ']';
    }
}