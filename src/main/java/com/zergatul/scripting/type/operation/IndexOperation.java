package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class IndexOperation {

    public final SType type;

    protected IndexOperation(SType type) {
        this.type = type;
    }

    public boolean canGet() {
        return false;
    }

    public boolean canSet() {
        return false;
    }

    // assumes [..., callee, index] on stack
    public abstract void compileGet(MethodVisitor visitor);

    // assumes [..., callee, index, value] on stack
    public abstract void compileSet(MethodVisitor visitor);
}