package com.zergatul.scripting.compiler;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;


public abstract class Variable extends Symbol {

    protected Variable(String name, SType type, TextRange definition) {
        super(name, type, definition);
    }

    public abstract boolean isConstant();

    public void compileInit(CompilerContext context, MethodVisitor visitor) {

    }

    public abstract void compileStore(CompilerContext context, MethodVisitor visitor);
}
