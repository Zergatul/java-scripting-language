package com.zergatul.scripting.compiler;

@FunctionalInterface
public interface CompileConsumer {
    void apply(CompilerMethodVisitor visitor) throws ScriptCompileException;
}
