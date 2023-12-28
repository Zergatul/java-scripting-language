package com.zergatul.scripting.compiler.variables;

import com.zergatul.scripting.compiler.types.SType;

public class FunctionEntry {

    private final String className;
    private final String identifier;
    private final SType[] parameters;
    private final SType returnType;

    public FunctionEntry(String className, String identifier, SType[] parameters, SType returnType) {
        this.className = className;
        this.identifier = identifier;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public String getClassName() {
        return className;
    }

    public String getIdentifier() {
        return identifier;
    }

    public SType[] getParameters() {
        return parameters;
    }

    public SType getReturnType() {
        return returnType;
    }

    public String getDescriptor() {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (SType type : parameters) {
            builder.append(type.getDescriptor());
        }
        builder.append(')');
        builder.append(returnType.getDescriptor());
        return builder.toString();
    }
}
