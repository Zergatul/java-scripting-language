package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

public abstract class PropertyReference extends MemberReference {

    public abstract SType getType();
    public abstract boolean canGet();
    public abstract boolean canSet();

    public void compileGet(MethodVisitor visitor) {
        throw new InternalException();
    }

    public void compileSet(MethodVisitor visitor) {
        throw new InternalException();
    }
}