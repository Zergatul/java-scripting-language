package com.zergatul.scripting.binding;

import com.zergatul.scripting.type.ConversionType;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.operation.CastOperation;

public record ConversionInfo(ConversionType type, CastOperation cast, MethodReference method) {

    public ConversionInfo(ConversionType type) {
        this(type, null, null);
    }

    public ConversionInfo(ConversionType type, CastOperation cast) {
        this(type, cast, null);
    }

    public ConversionInfo(ConversionType type, MethodReference method) {
        this(type, null, method);
    }
}
