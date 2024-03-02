package com.zergatul.scripting.old.compiler;

@FunctionalInterface
public interface RunCompileConsumer {
    void apply(CompilerMethodVisitor visitor) throws ScriptCompileException;
}