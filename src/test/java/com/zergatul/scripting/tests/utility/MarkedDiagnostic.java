package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.ErrorCode;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public final class MarkedDiagnostic {

    private final String mark;
    private final ErrorCode errorCode;
    private final Object[] parameters;

    public MarkedDiagnostic(String mark, ErrorCode errorCode, Object... parameters) {
        this.mark = mark;
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    public String mark() {
        return mark;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public Object[] parameters() {
        return parameters;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        MarkedDiagnostic that = (MarkedDiagnostic) obj;
        return  Objects.equals(this.mark, that.mark) &&
                Objects.equals(this.errorCode, that.errorCode) &&
                Arrays.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mark, errorCode, Arrays.hashCode(parameters));
    }

    @Override
    public String toString() {
        return  "MarkedDiagnostic[" +
                "mark=" + mark + ", " +
                "errorCode=" + errorCode + ", " +
                "parameters=" + Arrays.toString(parameters) + ']';
    }
}