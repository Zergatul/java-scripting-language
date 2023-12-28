package com.zergatul.scripting.compiler.types;

public abstract class SPrimitiveType extends SType {

    protected final Class<?> type;

    protected SPrimitiveType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> getJavaClass() {
        return type;
    }

    public abstract int getArrayTypeInst();
}