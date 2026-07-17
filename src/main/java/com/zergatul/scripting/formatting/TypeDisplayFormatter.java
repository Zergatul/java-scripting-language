package com.zergatul.scripting.formatting;

import com.zergatul.scripting.type.*;

import java.util.List;
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
        return switch (type) {
            case SArrayType array -> format(array.getElementsType()) + "[]";
            case SClassType classType -> classNameFormatter.apply(classType.getJavaClass());
            case SFunction function -> formatFunction(function);
            case SFuture future -> "Future<" + format(future.getUnderlying()) + ">";
            default -> type.toString();
        };
    }

    private String formatFunction(SFunction function) {
        StringBuilder builder = new StringBuilder("fn<");
        List<MethodParameter> parameters = function.getParameters();
        if (parameters.size() != 1) {
            builder.append('(');
        }
        for (int i = 0; i < parameters.size(); i++) {
            builder.append(format(parameters.get(i).type()));
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        if (parameters.size() != 1) {
            builder.append(')');
        }
        builder.append(" => ");
        builder.append(format(function.getReturnType()));
        builder.append('>');
        return builder.toString();
    }
}