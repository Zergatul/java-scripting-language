package com.zergatul.scripting.runtime;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "Type")
public class RuntimeType {

    public final Class<?> clazz;

    RuntimeType(Class<?> clazz) {
        this.clazz = clazz;
    }
}