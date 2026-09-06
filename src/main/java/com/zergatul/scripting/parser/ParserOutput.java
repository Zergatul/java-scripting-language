package com.zergatul.scripting.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;

import java.util.List;
import java.util.Objects;

public final class ParserOutput {

    private final String code;
    private final CompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;

    public ParserOutput(String code, CompilationUnitNode unit, List<DiagnosticMessage> diagnostics) {
        this.code = code;
        this.unit = unit;
        this.diagnostics = diagnostics;
    }

    public String code() {
        return code;
    }

    public CompilationUnitNode unit() {
        return unit;
    }

    public List<DiagnosticMessage> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ParserOutput that = (ParserOutput) obj;
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
        return  "ParserOutput[" +
                "code=" + code + ", " +
                "unit=" + unit + ", " +
                "diagnostics=" + diagnostics + ']';
    }
}
