package com.zergatul.scripting.binding;

import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.type.ConversionType;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.operation.CastOperation;
import org.jspecify.annotations.Nullable;

public record ConversionInfo(
        ConversionType type,
        @Nullable CastOperation cast,
        @Nullable MethodReference method,
        @Nullable Function function
) {

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
}