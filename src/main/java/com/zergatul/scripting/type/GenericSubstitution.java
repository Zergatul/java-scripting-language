package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;

public class GenericSubstitution {

    private final Map<GenericParameterKey, SJavaTypeArgument> mappings;

    private GenericSubstitution(Map<GenericParameterKey, SJavaTypeArgument> mappings) {
        this.mappings = mappings;
    }

    public static GenericSubstitution forClass(Class<?> clazz, List<SJavaTypeArgument> arguments) {
        TypeVariable<?>[] parameters = clazz.getTypeParameters();

        if (parameters.length != arguments.size()) {
            throw new InternalException("Generic arity mismatch.");
        }

        Map<GenericParameterKey, SJavaTypeArgument> mappings = new LinkedHashMap<>();

        for (int i = 0; i < parameters.length; i++) {
            mappings.put(
                    GenericParameterKey.from(parameters[i]),
                    arguments.get(i));
        }

        return new GenericSubstitution(mappings);
    }

    public Optional<SJavaTypeArgument> find(GenericParameterKey key) {
        throw new InternalException();
    }

    public SType resolveType(java.lang.reflect.Type type) {
        return switch (type) {
            case Class<?> clazz -> SType.fromJavaType(clazz);
            case TypeVariable<?> variable -> resolveVariable(variable);
            case ParameterizedType parameterized -> resolveParameterizedType(parameterized);
            case GenericArrayType array -> new SArrayType(resolveType(array.getGenericComponentType()));
            case WildcardType wildcard -> captureWildcard(wildcard);
            default -> throw new InternalException();
        };
    }

    public SJavaTypeArgument resolveArgument(java.lang.reflect.Type type) {
        throw new InternalException();
    }

    public GenericSubstitution combine(GenericSubstitution inner) {
        throw new InternalException();
    }

    private SType resolveParameterizedType(ParameterizedType type) {
        Class<?> rawClass = (Class<?>) type.getRawType();

        List<SJavaTypeArgument> arguments =
                Arrays.stream(type.getActualTypeArguments())
                        .map(this::resolveArgument)
                        .toList();

        return new SParameterizedJavaType(rawClass, arguments);
    }

    private SType resolveVariable(TypeVariable<?> variable) {
        throw new InternalException();
    }

    private SType captureWildcard(WildcardType wildcard) {
        throw new InternalException();
    }
}