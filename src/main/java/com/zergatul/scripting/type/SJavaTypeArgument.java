package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

public interface SJavaTypeArgument {

    static SJavaTypeArgument from(Type type) {
        return from(type, GenericSubstitution.EMPTY);
    }

    static SJavaTypeArgument from(Type type, GenericSubstitution substitution) {
        if (type instanceof Class<?> clazz) {
            return new SJavaExactTypeArgument(SType.fromJavaType(clazz));
        }
        if (type instanceof ParameterizedType parameterized) {
            return new SJavaExactTypeArgument(SType.fromJavaType(parameterized));
        }
        if (type instanceof WildcardType wildcard) {
            Type[] lower = wildcard.getLowerBounds();
            Type[] upper = wildcard.getUpperBounds();

            List<SType> upperTypes =
                    Arrays.stream(upper)
                            .map(substitution::resolveType)
                            .toList();

            List<SType> lowerTypes =
                    Arrays.stream(lower)
                            .map(substitution::resolveType)
                            .toList();

            if (upperTypes.size() == 1 && upperTypes.getFirst().equals(SJavaObject.instance) && lowerTypes.isEmpty()) {
                return new SJavaUnboundedTypeArgument();
            }

            return new SJavaWildcardTypeArgument(upperTypes, lowerTypes);
        }
        if (type instanceof TypeVariable<?> variable) {
            return substitution.resolveArgument(variable);
        }
        throw new InternalException();
    }
}