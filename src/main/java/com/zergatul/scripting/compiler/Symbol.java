package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

public abstract class Symbol {

    private final String name;
    private final SType type;

    protected Symbol(String name, SType type) {
        this.name = name;
        this.type = type;
    }

    public SType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public abstract void compileLoad(FunctionVisitor visitor);
}