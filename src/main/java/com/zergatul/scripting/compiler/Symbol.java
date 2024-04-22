package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

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

    public boolean canSet() {
        return false;
    }

    public abstract void compileLoad(CompilerContext context, MethodVisitor visitor);
}