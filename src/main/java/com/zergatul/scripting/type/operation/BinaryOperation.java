package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class BinaryOperation {

    public final SType type;

    protected BinaryOperation(SType type) {
        this.type = type;
    }

    public abstract void apply(MethodVisitor left, BufferedMethodVisitor right);
}