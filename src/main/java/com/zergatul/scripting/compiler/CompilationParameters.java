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
    private final boolean keepVariableNames;
    private final boolean debug;

    public CompilationParameters(Class<?> root) {
        this(root, false, false);
    }

    public CompilationParameters(Class<?> root, boolean keepVariableNames, boolean debug) {
        this.keepVariableNames = keepVariableNames;
        this.debug = debug;
        Arrays.stream(root.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .forEach(f -> addStaticVariable(new StaticFieldConstantStaticVariable(f.getName(), f)));
    }

    public boolean shouldKeepVariableNames() {
        return keepVariableNames;
    }

    public boolean isDebug() {
        return debug;
    }

    public CompilerContext getContext() {
        CompilerContext context = new CompilerContext();
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