package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class CastOperation {

    private final SType srcType;
    private final SType dstType;

    protected CastOperation(SType srcType, SType dstType) {
        this.srcType = srcType;
        this.dstType = dstType;
    }

    public SType getSrcType() {
        return srcType;
    }

    public SType getDstType() {
        return dstType;
    }

    public abstract void apply(MethodVisitor visitor);
}