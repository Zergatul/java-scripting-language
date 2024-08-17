package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InterfaceHelper;
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
    private final Class<?> functionalInterface;
    private final SType asyncReturnType;
    private final VisibilityChecker checker;
    private final boolean debug;

    public CompilationParameters(Class<?> root, Class<?> functionalInterface, SType asyncReturnType, VisibilityChecker checker, boolean debug) {
        if (!InterfaceHelper.isFuncInterface(functionalInterface)) {
            throw new InternalException(String.format("%s is not a functional interface", functionalInterface));
        }

        this.functionalInterface = functionalInterface;
        this.asyncReturnType = asyncReturnType;
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

    public Class<?> getFunctionalInterface() {
        return functionalInterface;
    }

    public CompilerContext getContext() {
        CompilerContext context = new CompilerContext(getReturnType());
        context.setChecker(checker);
        for (StaticVariable variable : staticVariables) {
            context.addStaticVariable(variable);
        }
        return context;
    }

    public void addFunctionalInterfaceParameters(CompilerContext context) {
        Parameter[] parameters = InterfaceHelper.getFuncInterfaceMethod(functionalInterface).getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.addExternalParameter(parameters[i].getName(), SType.fromJavaType(parameters[i].getType()), i);
        }
    }

    public SType getReturnType() {
        if (asyncReturnType != null) {
            return asyncReturnType;
        } else {
            return SType.fromJavaType(InterfaceHelper.getFuncInterfaceMethod(functionalInterface).getReturnType());
        }
    }

    public boolean isAsync() {
        return asyncReturnType != null;
    }

    protected void addStaticVariable(StaticVariable variable) {
        if (staticVariables.stream().anyMatch(v -> v.getName().equals(variable.getName()))) {
            throw new InternalException();
        }

        staticVariables.add(variable);
    }

    private Method getMethod(Class<?> functionalInterface) {
        if (!functionalInterface.isInterface()) {
            throw new InternalException();
        }
        List<Method> methods = Arrays.stream(functionalInterface.getMethods()).filter(m -> !m.isDefault()).toList();
        if (methods.size() != 1) {
            throw new InternalException();
        }
        return methods.get(0);
    }
}