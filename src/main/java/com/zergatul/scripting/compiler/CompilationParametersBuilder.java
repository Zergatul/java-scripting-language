package com.zergatul.scripting.compiler;

public class CompilationParametersBuilder {

    private Class<?> root;
    private Class<?> functionalInterface;
    private boolean debug;
    private VisibilityChecker checker;

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

    public CompilationParametersBuilder setDebug() {
        this.debug = true;
        return this;
    }

    public CompilationParametersBuilder setVisibilityChecker(VisibilityChecker checker) {
        this.checker = checker;
        return this;
    }

    public CompilationParameters build() {
        return new CompilationParameters(root, functionalInterface, checker, debug);
    }
}