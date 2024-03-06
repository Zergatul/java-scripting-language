package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

public class LocalVariable extends Variable {

    private final int stackIndex;

    public LocalVariable(String name, SType type, int stackIndex) {
        super(name, type);
        this.stackIndex = stackIndex;
    }

    public int getStackIndex() {
        return stackIndex;
    }

    @Override
    public void compileLoad(FunctionVisitor visitor) {

    }

    @Override
    public void compileStore(FunctionVisitor visitor) {

    }
}