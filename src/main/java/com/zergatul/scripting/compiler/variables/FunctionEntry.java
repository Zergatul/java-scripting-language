package com.zergatul.scripting.compiler.variables;

import com.zergatul.scripting.compiler.types.SType;

public class FunctionEntry {

    private final String className;
    private final String identifier;
    private final SType[] arguments;
    private final SType returnType;

    public FunctionEntry(String className, String identifier, SType[] arguments, SType returnType) {
        this.className = className;
        this.identifier = identifier;
        this.arguments = arguments;
        this.returnType = returnType;
    }

    public String getClassName() {
        return className;
    }

    public SType[] getArguments() {
        return arguments;
    }

    public SType getReturnType() {
        return returnType;
    }
}
