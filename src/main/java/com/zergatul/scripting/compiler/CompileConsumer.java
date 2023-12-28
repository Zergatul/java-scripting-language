package com.zergatul.scripting.compiler;

import org.objectweb.asm.ClassWriter;

@FunctionalInterface
public interface CompileConsumer {
    void apply(
            ClassWriter classWriter,
            CompilerMethodVisitor constructorVisitor,
            CompilerMethodVisitor runVisitor) throws ScriptCompileException;
}