package com.zergatul.scripting.compiler;

public class ClassLoaderContext {

    private final DynamicCompilerClassLoader classLoader;
    private int nextIndex;

    public ClassLoaderContext() {
        this.classLoader = new DynamicCompilerClassLoader();
        this.nextIndex = 1;
    }

    public Class<?> defineClass(String name, byte[] code) {
        return classLoader.defineClass(name, code);
    }

    public int getNextUniqueIndex() {
        return nextIndex++;
    }
}