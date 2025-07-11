package com.zergatul.scripting.runtime;

import java.util.HashMap;
import java.util.Map;

public class RuntimeTypes {

    private static final Map<Class<?>, RuntimeType> cache = new HashMap<>();

    public static RuntimeType get(Class<?> clazz) {
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        } else {
            RuntimeType type = new RuntimeType(clazz);
            cache.put(clazz, type);
            return type;
        }
    }
}