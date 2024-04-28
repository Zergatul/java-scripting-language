package com.zergatul.scripting.compiler;

public class DynamicCompilerClassLoader extends ClassLoader {

    public DynamicCompilerClassLoader() {
        super(DynamicCompilerClassLoader.class.getClassLoader());
    }

    public Class<?> defineClass(String name, byte[] code) {
        return defineClass(name, code, 0, code.length);
    }
}