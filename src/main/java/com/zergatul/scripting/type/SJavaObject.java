package com.zergatul.scripting.type;

import org.jspecify.annotations.Nullable;

public class SJavaObject extends SClassType {

    public static final SJavaObject instance = new SJavaObject();

    private SJavaObject() {
        super(Object.class);
    }

    @Override
    public @Nullable SType getBaseType() {
        return null;
    }
}