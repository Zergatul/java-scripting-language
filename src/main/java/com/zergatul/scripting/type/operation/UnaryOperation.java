package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class UnaryOperation {

    public final SType type;

    protected UnaryOperation(SType type) {
        this.type = type;
    }

    public abstract void apply(MethodVisitor visitor);
}