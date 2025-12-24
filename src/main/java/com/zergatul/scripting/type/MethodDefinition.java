package com.zergatul.scripting.type;

import java.util.List;

public record MethodDefinition(SType returnType, String name, MethodParameter... parameters) {

    public boolean matches(MethodReference method) {
        if (!method.getReturn().equals(returnType)) {
            return false;
        }
        if (!method.getName().equals(name)) {
            return false;
        }

        List<MethodParameter> methodParameters = method.getParameters();
        if (methodParameters.size() != parameters.length) {
            return false;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (methodParameters.get(i).type().equals(parameters[i].type())) {
                return false;
            }
        }

        return true;
    }
}