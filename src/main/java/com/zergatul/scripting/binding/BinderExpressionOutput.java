package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.nodes.BoundExpressionUnitNode;
import com.zergatul.scripting.compiler.CompilerContext;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class BinderExpressionOutput {

    private final String code;
    private final BoundExpressionUnitNode unit;
    private final CompilerContext context;
    private final List<DiagnosticMessage> diagnostics;

    public BinderExpressionOutput(
            String code,
            BoundExpressionUnitNode unit,
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

    public BoundExpressionUnitNode unit() {
        return unit;
    }

    public CompilerContext context() {
        return context;
    }

    public List<DiagnosticMessage> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BinderExpressionOutput that = (BinderExpressionOutput) obj;
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
        return  "BinderExpressionOutput[" +
                "code=" + code + ", " +
                "unit=" + unit + ", " +
                "context=" + context + ", " +
                "diagnostics=" + diagnostics + ']';
    }
}