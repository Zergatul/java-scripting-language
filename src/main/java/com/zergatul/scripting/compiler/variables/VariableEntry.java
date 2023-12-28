package com.zergatul.scripting.compiler.variables;

import com.zergatul.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.scripting.compiler.types.SType;

public abstract class VariableEntry {

    protected final SType type;

    protected VariableEntry(SType type) {
        this.type = type;
    }

    public SType getType() {
        return type;
    }

    public abstract void compileLoad(CompilerMethodVisitor visitor);
    public abstract void compileStore(CompilerMethodVisitor visitor);
    public abstract void compileIncrement(CompilerMethodVisitor visitor, int value);
}