package com.zergatul.scripting.type;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class MethodDefinition {

    private final SType returnType;
    private final String name;
    private final MethodParameter[] parameters;

    public MethodDefinition(SType returnType, String name, MethodParameter... parameters) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
    }

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

    public SType returnType() {
        return returnType;
    }

    public String name() {
        return name;
    }

    public MethodParameter[] parameters() {
        return parameters;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        MethodDefinition that = (MethodDefinition) obj;
        return  Objects.equals(this.returnType, that.returnType) &&
                Objects.equals(this.name, that.name) &&
                Arrays.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, name, Arrays.hashCode(parameters));
    }

    @Override
    public String toString() {
        return  "MethodDefinition[" +
                "returnType=" + returnType + ", " +
                "name=" + name + ", " +
                "parameters=" + Arrays.toString(parameters) + ']';
    }
}