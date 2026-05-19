package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class NoOpCastOperation extends CastOperation {

    public NoOpCastOperation(SType srcType, SType dstType) {
        super(srcType, dstType);
    }

    @Override
    public void apply(MethodVisitor visitor) {}
}
