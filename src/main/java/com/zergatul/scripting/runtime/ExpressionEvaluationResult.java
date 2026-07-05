package com.zergatul.scripting.runtime;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public record ExpressionEvaluationResult(
        boolean ok,
        boolean hasValue,
        String type,
        String javaType,
        String value,
        List<DiagnosticMessage> diagnostics
) {
    public static ExpressionEvaluationResult fromVoid() {
        return new ExpressionEvaluationResult(
                true,
                false,
                SVoidType.instance.toString(),
                Void.class.getCanonicalName(),
                "Expression returned no value",
                List.of());
    }

    public static ExpressionEvaluationResult fromNull(Class<?> type) {
        return new ExpressionEvaluationResult(
                true,
                true,
                SType.fromJavaType(type).toString(),
                type.getCanonicalName(),
                "null",
                List.of());
    }

    public static ExpressionEvaluationResult fromValue(Class<?> type, Object value) {
        return new ExpressionEvaluationResult(
                true,
                true,
                SType.fromJavaType(type).toString(),
                type.getCanonicalName(),
                value.toString(),
                List.of());
    }

    public static ExpressionEvaluationResult fromException(Throwable throwable) {
        return new ExpressionEvaluationResult(
                false,
                false,
                SType.fromJavaType(throwable.getClass()).toString(),
                throwable.getClass().getCanonicalName(),
                throwable.toString(),
                List.of());
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
}