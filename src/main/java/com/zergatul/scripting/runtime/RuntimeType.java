package com.zergatul.scripting.runtime;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import com.zergatul.scripting.type.SType;

@CustomType(name = "Type")
public class RuntimeType {

    private final Class<?> clazz;

    RuntimeType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getJavaType() {
        return clazz;
    }

    @Getter(name = "name")
    public String getName() {
        return SType.fromJavaType(clazz).toString();
    }
}