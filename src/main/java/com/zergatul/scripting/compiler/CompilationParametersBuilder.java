package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;

import java.util.ArrayList;
import java.util.List;

public class CompilationParametersBuilder {

    private Class<?> root;
    private Class<?> functionalInterface;
    private SType asyncReturnType;
    private List<Class<?>> customTypes;
    private VisibilityChecker checker;
    private String classNamePrefix;
    private String sourceFile;
    private boolean emitLineNumbers;
    private boolean debug;

    public CompilationParametersBuilder() {
        functionalInterface = Runnable.class;
        customTypes = List.of();
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

    public CompilationParametersBuilder addCustomType(Class<?> clazz) {
        if (this.customTypes.isEmpty()) {
            this.customTypes = new ArrayList<>();
        }
        this.customTypes.add(clazz);
        return this;
    }

    public CompilationParametersBuilder setVisibilityChecker(VisibilityChecker checker) {
        this.checker = checker;
        return this;
    }

    public CompilationParametersBuilder setClassNamePrefix(String classNamePrefix) {
        this.classNamePrefix = classNamePrefix;
        return this;
    }

    public CompilationParametersBuilder setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
        return this;
    }

    public CompilationParametersBuilder setLineNumbers(boolean value) {
        this.emitLineNumbers = value;
        return this;
    }

    public CompilationParametersBuilder setDebug() {
        this.debug = true;
        return this;
    }

    public CompilationParameters build() {
        return new CompilationParameters(root, functionalInterface, asyncReturnType, customTypes, checker, classNamePrefix, sourceFile, emitLineNumbers, debug);
    }
}