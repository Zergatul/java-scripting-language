package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

public abstract class SValueType extends SType {

    protected final Class<?> type;

    protected SValueType(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean isPredefined() {
        return true;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public Class<?> getJavaClass() {
        return type;
    }

    public abstract int getArrayTypeInst();
    public abstract SBoxedType getBoxed();
    public abstract void compileBoxing(MethodVisitor visitor);
    public abstract void compileUnboxing(MethodVisitor visitor);
}