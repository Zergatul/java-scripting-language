package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public interface SJavaTypeArgument {

    static SJavaTypeArgument from(Type type) {
        if (type instanceof Class<?> clazz) {
            return new SJavaExactTypeArgument(SType.fromJavaType(clazz));
        }
        if (type instanceof ParameterizedType parameterized) {
            return new SJavaExactTypeArgument(SType.fromJavaType(parameterized));
        }
        if (type instanceof WildcardType wildcard) {
            Type[] lower = wildcard.getLowerBounds();
            Type[] upper = wildcard.getUpperBounds();
            if (upper.length == 1 && upper[0] == Object.class && lower.length == 0) {
                return new SJavaUnboundedTypeArgument();
            }

            return new SJavaWildcardTypeArgument(
                    Arrays.stream(upper).map(SType::fromJavaType).toList(),
                    Arrays.stream(lower).map(SType::fromJavaType).toList());
        }
        throw new InternalException();
    }
}