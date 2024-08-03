package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

public class CompilationParametersBuilder {

    private Class<?> root;
    private Class<?> functionalInterface;
    private SType asyncReturnType;
    private VisibilityChecker checker;
    private boolean debug;

    public CompilationParametersBuilder() {
        functionalInterface = Runnable.class;
    }

    public CompilationParametersBuilder setRoot(Class<?> root) {
        this.root = root;
        return this;
    }

    public CompilationParametersBuilder setInterface(Class<?> functionalInterface) {
        this.functionalInterface = functionalInterface;
        return this;
    }

    public CompilationParametersBuilder setAsyncReturnType(SType type) {
        this.asyncReturnType = type;
        return this;
    }

    public CompilationParametersBuilder setVisibilityChecker(VisibilityChecker checker) {
        this.checker = checker;
        return this;
    }

    public CompilationParametersBuilder setDebug() {
        this.debug = true;
        return this;
    }

    public CompilationParameters build() {
        return new CompilationParameters(root, functionalInterface, asyncReturnType, checker, debug);
    }
}