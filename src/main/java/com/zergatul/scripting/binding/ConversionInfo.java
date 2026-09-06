package com.zergatul.scripting.binding;

import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.type.ConversionType;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.operation.CastOperation;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ConversionInfo {

    private final ConversionType type;
    private final @Nullable CastOperation cast;
    private final @Nullable MethodReference method;
    private final @Nullable Function function;

    public ConversionInfo(
            ConversionType type,
            @Nullable CastOperation cast,
            @Nullable MethodReference method,
            @Nullable Function function
    ) {
        this.type = type;
        this.cast = cast;
        this.method = method;
        this.function = function;
    }

    public ConversionInfo(ConversionType type) {
        this(type, null, null, null);
    }

    public ConversionInfo(ConversionType type, CastOperation cast) {
        this(type, cast, null, null);
    }

    public ConversionInfo(ConversionType type, MethodReference method) {
        this(type, null, method, null);
    }

    public ConversionInfo(ConversionType type, Function function) {
        this(type, null, null, function);
    }

    public ConversionType type() {
        return type;
    }

    public @Nullable CastOperation cast() {
        return cast;
    }

    public @Nullable MethodReference method() {
        return method;
    }

    public @Nullable Function function() {
        return function;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ConversionInfo that = (ConversionInfo) obj;
        return  Objects.equals(this.type, that.type) &&
                Objects.equals(this.cast, that.cast) &&
                Objects.equals(this.method, that.method) &&
                Objects.equals(this.function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, cast, method, function);
    }

    @Override
    public String toString() {
        return  "ConversionInfo[" +
                "type=" + type + ", " +
                "cast=" + cast + ", " +
                "method=" + method + ", " +
                "function=" + function + ']';
    }
}