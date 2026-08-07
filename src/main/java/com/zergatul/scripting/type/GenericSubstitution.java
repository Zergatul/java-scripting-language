package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

import java.lang.reflect.*;
import java.util.*;

public class GenericSubstitution {

    public static final GenericSubstitution EMPTY = new GenericSubstitution(Map.of());

    private final Map<TypeVariable<?>, SJavaTypeArgument> mappings;

    private GenericSubstitution(Map<TypeVariable<?>, SJavaTypeArgument> mappings) {
        this.mappings = mappings;
    }

    public static GenericSubstitution forRawClass(Class<?> clazz) {
        TypeVariable<?>[] parameters = clazz.getTypeParameters();

        Map<TypeVariable<?>, SJavaTypeArgument> mappings = new LinkedHashMap<>();
        for (TypeVariable<?> parameter : parameters) {
            mappings.put(parameter, new SJavaExactTypeArgument(SType.fromJavaType(parameter)));
        }

        return new GenericSubstitution(mappings);
    }

    public static GenericSubstitution forClass(Class<?> clazz, List<SJavaTypeArgument> arguments) {
        TypeVariable<?>[] parameters = clazz.getTypeParameters();

        if (parameters.length != arguments.size()) {
            throw new InternalException("Generic arity mismatch.");
        }

        Map<TypeVariable<?>, SJavaTypeArgument> mappings = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            mappings.put(parameters[i], arguments.get(i));
        }

        return new GenericSubstitution(mappings);
    }

    public Optional<SJavaTypeArgument> find(GenericDeclaration key) {
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
        if (type instanceof TypeVariable<?> variable) {
            SJavaTypeArgument argument = mappings.get(variable);
            if (argument != null) {
                return argument;
            }

            // method-owned or otherwise unresolved variable: use its erasure for now
            return new SJavaExactTypeArgument(SType.fromJavaType(variable));
        } else {
            return SJavaTypeArgument.from(type, this);
        }
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
        SJavaTypeArgument argument = mappings.get(variable);
        if (argument instanceof SJavaExactTypeArgument exact) {
            return exact.type();
        }
        if (argument == null) {
            return SType.fromJavaType(variable);
        }

        throw new InternalException();
    }

    private SJavaTypeVariable resolveVariable2(TypeVariable<?> variable) {
        GenericDeclaration declaration = variable.getGenericDeclaration();
        throw new InternalException();
    }

    private SType captureWildcard(WildcardType wildcard) {
        throw new InternalException();
    }
}