package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class CastOperation {

    public final SType type;

    protected CastOperation(SType type) {
        this.type = type;
    }

    public abstract void apply(MethodVisitor visitor);
}