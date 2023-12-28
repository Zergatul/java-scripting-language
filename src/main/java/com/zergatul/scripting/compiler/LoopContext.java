package com.zergatul.scripting.compiler;

public class LoopContext {

    private final RunCompileConsumer continueConsumer;
    private final RunCompileConsumer breakConsumer;

    public LoopContext(RunCompileConsumer continueConsumer, RunCompileConsumer breakConsumer) {
        this.continueConsumer = continueConsumer;
        this.breakConsumer = breakConsumer;
    }

    public void compileContinue(CompilerMethodVisitor visitor) throws ScriptCompileException {
        continueConsumer.apply(visitor);
    }

    public void compileBreak(CompilerMethodVisitor visitor) throws ScriptCompileException {
        breakConsumer.apply(visitor);
    }
}