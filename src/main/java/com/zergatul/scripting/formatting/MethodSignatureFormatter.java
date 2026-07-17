package com.zergatul.scripting.formatting;

import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.MethodReference;

import java.util.List;

public class MethodSignatureFormatter {

    private final TypeDisplayFormatter typeFormatter;

    public MethodSignatureFormatter(TypeDisplayFormatter typeFormatter) {
        this.typeFormatter = typeFormatter;
    }

    public String format(MethodReference method) {
        return typeFormatter.format(method.getReturn()) + " " +
                typeFormatter.format(method.getOwner()) + "." +
                method.getName() +
                formatParameters(method.getParameters());
    }

    public String formatParameters(List<MethodParameter> parameters) {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < parameters.size(); i++) {
            MethodParameter parameter = parameters.get(i);
            builder.append(typeFormatter.format(parameter.type()));
            builder.append(' ');
            builder.append(parameter.name());
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(')');
        return builder.toString();
    }
}