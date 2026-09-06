package com.zergatul.scripting.runtime;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public final class ExpressionEvaluationResult {

    private final boolean ok;
    private final boolean hasValue;
    private final String type;
    private final String javaType;
    private final String value;
    private final List<DiagnosticMessage> diagnostics;

    public ExpressionEvaluationResult(
            boolean ok,
            boolean hasValue,
            String type,
            String javaType,
            String value,
            List<DiagnosticMessage> diagnostics
    ) {
        this.ok = ok;
        this.hasValue = hasValue;
        this.type = type;
        this.javaType = javaType;
        this.value = value;
        this.diagnostics = diagnostics;
    }

    public static ExpressionEvaluationResult fromVoid() {
        return new ExpressionEvaluationResult(
                true,
                false,
                SVoidType.instance.toString(),
                Void.class.getCanonicalName(),
                "Expression returned no value",
                Lists.of());
    }

    public static ExpressionEvaluationResult fromNull(Class<?> type) {
        return new ExpressionEvaluationResult(
                true,
                true,
                SType.fromJavaType(type).toString(),
                type.getCanonicalName(),
                "null",
                Lists.of());
    }

    public static ExpressionEvaluationResult fromValue(Class<?> type, Object value) {
        return new ExpressionEvaluationResult(
                true,
                true,
                SType.fromJavaType(type).toString(),
                type.getCanonicalName(),
                value.toString(),
                Lists.of());
    }

    public static ExpressionEvaluationResult fromException(Throwable throwable) {
        return new ExpressionEvaluationResult(
                false,
                false,
                SType.fromJavaType(throwable.getClass()).toString(),
                throwable.getClass().getCanonicalName(),
                throwable.toString(),
                Lists.of());
    }

    public static ExpressionEvaluationResult fromDiagnostics(List<DiagnosticMessage> diagnostics) {
        return new ExpressionEvaluationResult(
                false,
                false,
                "",
                "",
                "",
                diagnostics);
    }

    public boolean ok() {
        return ok;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public String type() {
        return type;
    }

    public String javaType() {
        return javaType;
    }

    public String value() {
        return value;
    }

    public List<DiagnosticMessage> diagnostics() {
        return diagnostics;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ExpressionEvaluationResult that = (ExpressionEvaluationResult) obj;
        return  this.ok == that.ok &&
                this.hasValue == that.hasValue &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.javaType, that.javaType) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.diagnostics, that.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ok, hasValue, type, javaType, value, diagnostics);
    }

    @Override
    public String toString() {
        return  "ExpressionEvaluationResult[" +
                "ok=" + ok + ", " +
                "hasValue=" + hasValue + ", " +
                "type=" + type + ", " +
                "javaType=" + javaType + ", " +
                "value=" + value + ", " +
                "diagnostics=" + diagnostics + ']';
    }
}