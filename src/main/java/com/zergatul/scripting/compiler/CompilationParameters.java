package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.symbols.StaticVariable;
import com.zergatul.scripting.type.SType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompilationParameters {

    private final List<StaticVariable> staticVariables = new ArrayList<>();
    private final VisibilityChecker checker;
    private final boolean debug;

    public CompilationParameters(Class<?> root, VisibilityChecker checker, boolean debug) {
        this.checker = checker;
        this.debug = debug;
        Arrays.stream(root.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .forEach(f -> addStaticVariable(new StaticFieldConstantStaticVariable(f.getName(), f)));
    }

    public boolean isDebug() {
        return debug;
    }

    public CompilerContext getContext() {
        CompilerContext context = new CompilerContext();
        context.setChecker(checker);
        for (StaticVariable variable : staticVariables) {
            context.addStaticVariable(variable);
        }
        return context;
    }

    public CompilerContext getContext(Class<?> functionalInterface) {
        CompilerContext context = getContext();

        if (!functionalInterface.isInterface()) {
            throw new InternalException();
        }
        List<Method> methods = Arrays.stream(functionalInterface.getMethods()).filter(m -> !m.isDefault()).toList();
        if (methods.size() != 1) {
            throw new InternalException();
        }
        Parameter[] parameters = methods.get(0).getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.addExternalParameter(parameters[i].getName(), SType.fromJavaType(parameters[i].getType()), i);
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