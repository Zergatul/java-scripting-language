package com.zergatul.scripting.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.parser.nodes.ExpressionUnitNode;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class ParserExpressionOutput {

    private final String code;
    private final ExpressionUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;

    public ParserExpressionOutput(String code, ExpressionUnitNode unit, List<DiagnosticMessage> diagnostics) {
        this.code = code;
        this.unit = unit;
        this.diagnostics = diagnostics;
    }

    public String code() {
        return code;
    }

    public ExpressionUnitNode unit() {
        return unit;
    }

    public List<DiagnosticMessage> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ParserExpressionOutput that = (ParserExpressionOutput) obj;
        return  Objects.equals(this.code, that.code) &&
                Objects.equals(this.unit, that.unit) &&
                Objects.equals(this.diagnostics, that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, unit, diagnostics);
    }

    @Override
    public String toString() {
        return  "ParserExpressionOutput[" +
                "code=" + code + ", " +
                "unit=" + unit + ", " +
                "diagnostics=" + diagnostics + ']';
    }
}