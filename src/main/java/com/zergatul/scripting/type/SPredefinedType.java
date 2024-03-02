package com.zergatul.scripting.type;

public abstract class SPredefinedType extends SType {

    protected final Class<?> type;

    protected SPredefinedType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Class<?> getJavaClass() {
        return type;
    }

    public abstract int getArrayTypeInst();
}