package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class SJavaTypeVariable extends SReferenceType {

    private final GenericParameterKey key;
    private final String name;
    private final List<SType> upperBounds;
    private final Class<?> erasedClass;

    public SJavaTypeVariable(GenericParameterKey key, String name, List<SType> upperBounds, Class<?> erasedClass) {
        this.key = key;
        this.name = name;
        this.upperBounds = upperBounds;
        this.erasedClass = erasedClass;
    }

    @Override
    public Class<?> getJavaClass() {
        return erasedClass;
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {

    }
}