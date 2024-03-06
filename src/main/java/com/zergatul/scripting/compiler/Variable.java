package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

public abstract class Variable extends Symbol {

    protected Variable(String name, SType type) {
        super(name, type);
    }

    public abstract void compileStore(FunctionVisitor visitor);
}
