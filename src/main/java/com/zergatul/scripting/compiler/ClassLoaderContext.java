package com.zergatul.scripting.compiler;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassWriter;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

public class ClassLoaderContext {

    private final DynamicCompilerClassLoader classLoader;
    private final TypeHierarchy hierarchy;
    private int nextIndex;

    public ClassLoaderContext() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClassLoaderContext(@Nullable ClassLoader javaTypeClassLoader) {
        this.classLoader = new DynamicCompilerClassLoader();
        this.hierarchy = new TypeHierarchy(javaTypeClassLoader);
        this.nextIndex = 1;
    }

    public void registerClass(String name, int access, String superName, @Nullable String[] interfaces) {
        hierarchy.register(name, (access & ACC_INTERFACE) != 0, superName, interfaces);
    }

    public ClassWriter createClassWriter(int version, int access, String name, String superName, @Nullable String[] interfaces) {
        registerClass(name, access, superName, interfaces);
        ClassWriter writer = new CompilerClassWriter(hierarchy);
        writer.visit(version, access, name, null, superName, interfaces);
        return writer;
    }

    public Class<?> defineClass(String name, byte[] code) {
        return classLoader.defineClass(name, code);
    }

    public int getNextUniqueIndex() {
        return nextIndex++;
    }
}