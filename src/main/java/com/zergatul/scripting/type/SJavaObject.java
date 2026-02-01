package com.zergatul.scripting.type;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class SJavaObject extends SClassType {

    public static final SJavaObject instance = new SJavaObject();

    private SJavaObject() {
        super(Object.class);
    }

    @Override
    public @Nullable SType getBaseType() {
        return null;
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return Arrays.stream(Object.class.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(NativeMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }
}