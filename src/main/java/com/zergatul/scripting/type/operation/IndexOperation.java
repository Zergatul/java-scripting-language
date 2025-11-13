package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class IndexOperation {

    public final SType indexType;
    public final SType returnType;

    protected IndexOperation(SType indexType, SType returnType) {
        this.indexType = indexType;
        this.returnType = returnType;
    }

    public boolean canGet() {
        return false;
    }

    public boolean canSet() {
        return false;
    }

    // assumes [..., callee, index] on stack
    public void compileGet(MethodVisitor visitor) {
        throw new InternalException();
    }

    // assumes [..., callee, index, value] on stack
    public void compileSet(MethodVisitor visitor) {
        throw new InternalException();
    }
}