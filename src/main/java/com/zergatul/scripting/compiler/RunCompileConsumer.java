package com.zergatul.scripting.compiler;

@FunctionalInterface
public interface RunCompileConsumer {
    void apply(CompilerMethodVisitor visitor) throws ScriptCompileException;
}