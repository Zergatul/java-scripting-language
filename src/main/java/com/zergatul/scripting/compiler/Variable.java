package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class Variable extends Symbol {

    protected Variable(String name, SType type) {
        super(name, type);
    }

    public abstract boolean isConstant();

    public abstract void compileStore(MethodVisitor visitor);
}
