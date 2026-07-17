package com.zergatul.scripting.formatting;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TypeDisplayFormatter {

    private final Function<Class<?>, String> classNameFormatter;

    public TypeDisplayFormatter() {
        this(Class::getName);
    }

    public TypeDisplayFormatter(Function<Class<?>, String> classNameFormatter) {
        this.classNameFormatter = classNameFormatter;
    }

    public String format(SType type) {
        return format(type, new HashSet<>());
    }

    private String format(SType type, Set<Class<?>> expanding) {
        return switch (type) {
            case SArrayType array -> format(array.getElementsType(), expanding) + "[]";
            case SClassType classType -> formatClassType(classType, expanding);
            case SFunction function -> formatFunction(function, expanding);
            case SFuture future -> "Future<" + format(future.getUnderlying(), expanding) + ">";
            case SSyntheticInterface syntheticInterface -> formatSyntheticInterface(syntheticInterface, expanding);
            default -> type.toString();
        };
    }

    private String formatClassType(SClassType classType, Set<Class<?>> expanding) {
        SFunction callable = classType.getCallableType();
        if (callable == null) {
            return formatClassName(classType);
        }

        Class<?> clazz = classType.getJavaClass();
        if (!expanding.add(clazz)) {
            return formatClassName(classType);
        }

        try {
            return format(callable, expanding);
        } finally {
            expanding.remove(clazz);
        }
    }

    private String formatClassName(SClassType classType) {
        return classNameFormatter.apply(classType.getJavaClass());
    }

    private String formatFunction(SFunction function, Set<Class<?>> expanding) {
        StringBuilder builder = new StringBuilder("fn<");
        List<MethodParameter> parameters = function.getParameters();
        if (parameters.size() != 1) {
            builder.append('(');
        }
        for (int i = 0; i < parameters.size(); i++) {
            builder.append(format(parameters.get(i).type(), expanding));
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        if (parameters.size() != 1) {
            builder.append(')');
        }
        builder.append(" => ");
        builder.append(format(function.getReturnType(), expanding));
        builder.append('>');
        return builder.toString();
    }

    private String formatSyntheticInterface(SSyntheticInterface syntheticInterface, Set<Class<?>> expanding) {
        MethodDefinition definition = syntheticInterface.getDefinition();
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        builder.append(format(definition.returnType(), expanding));
        builder.append(" ");
        builder.append(definition.name());
        builder.append("(");
        if (definition.parameters().length > 0) {
            // most likely we will not need this
            throw new InternalException();
        }
        builder.append(")");
        builder.append(" }");
        return builder.toString();
    }
}