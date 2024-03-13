package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompilationParameters {

    private final List<StaticVariable> staticVariables = new ArrayList<>();

    public CompilationParameters() {

    }

    public CompilationParameters(Class<?> root) {
        Arrays.stream(root.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .forEach(f -> addStaticVariable(new StaticFieldConstantStaticVariable(f.getName(), f)));
    }

    public CompilerContext getContext() {
        CompilerContext context = new CompilerContext();
        for (StaticVariable variable : staticVariables) {
            context.addStaticVariable(variable);
        }
        return context;
    }

    protected void addStaticVariable(StaticVariable variable) {
        if (staticVariables.stream().anyMatch(v -> v.getName().equals(variable.getName()))) {
            throw new InternalException();
        }

        staticVariables.add(variable);
    }
}