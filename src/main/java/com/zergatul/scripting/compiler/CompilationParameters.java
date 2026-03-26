package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InterfaceHelper;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.symbols.ImmutableSymbolRef;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.symbols.StaticVariable;
import com.zergatul.scripting.type.CustomType;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CompilationParameters {

    private final List<StaticVariable> staticVariables = new ArrayList<>();
    private final Class<?> functionalInterface;
    private final @Nullable SType asyncReturnType;
    private final List<Class<?>> customTypes;
    private final @Nullable JavaInteropPolicy interopPolicy;
    private final @Nullable MethodUsagePolicy methodUsagePolicy;
    private final @Nullable String mainClassName;
    private final @Nullable String sourceFile;

    private final boolean emitLineNumbers;
    private final boolean emitVariableNames;
    private final boolean debug;

    public CompilationParameters(
            Class<?> root,
            Class<?> functionalInterface,
            @Nullable SType asyncReturnType,
            List<Class<?>> customTypes,
            @Nullable JavaInteropPolicy interopPolicy,
            @Nullable MethodUsagePolicy methodUsagePolicy,
            @Nullable String mainClassName,
            @Nullable String sourceFile,
            boolean emitLineNumbers,
            boolean emitVariableNames,
            boolean debug
    ) {
        if (!InterfaceHelper.isFuncInterface(functionalInterface)) {
            throw new InternalException(String.format("%s is not a functional interface.", functionalInterface));
        }

        for (Class<?> type : customTypes) {
            if (type.getAnnotation(CustomType.class) == null) {
                throw new InternalException(String.format("%s is not a valid custom type.", type.getName()));
            }
        }

        this.functionalInterface = functionalInterface;
        this.asyncReturnType = asyncReturnType;
        this.customTypes = addPredefinedTypes(customTypes);
        this.interopPolicy = interopPolicy;
        this.methodUsagePolicy = methodUsagePolicy;
        this.mainClassName = mainClassName;
        this.sourceFile = sourceFile;
        this.emitLineNumbers = emitLineNumbers;
        this.emitVariableNames = emitVariableNames;
        this.debug = debug;
        Arrays.stream(root.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .forEach(f -> addStaticVariable(new StaticFieldConstantStaticVariable(f.getName(), f)));
    }

    public String getMainClassName() {
        return mainClassName != null ? mainClassName : "Script";
    }

    public @Nullable String getSourceFile() {
        return sourceFile;
    }

    public boolean shouldEmitLineNumbers() {
        return emitLineNumbers;
    }

    public boolean shouldEmitVariableNames() {
        return emitVariableNames;
    }

    public boolean isDebug() {
        return debug;
    }

    public Class<?> getFunctionalInterface() {
        return functionalInterface;
    }

    public CompilerContext getContext() {
        CompilerContext context = CompilerContext.create(getReturnType(), isAsync());
        context.setInteropPolicy(interopPolicy);
        for (StaticVariable variable : staticVariables) {
            context.addStaticSymbol(variable.getName(), new ImmutableSymbolRef(variable));
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

    public List<Class<?>> getCustomTypes() {
        return customTypes;
    }

    public @Nullable JavaInteropPolicy getInteropPolicy() {
        return interopPolicy;
    }

    public @Nullable MethodUsagePolicy getMethodUsagePolicy() {
        return methodUsagePolicy;
    }

    protected void addStaticVariable(StaticVariable variable) {
        if (staticVariables.stream().anyMatch(v -> v.getName().equals(variable.getName()))) {
            throw new InternalException();
        }

        staticVariables.add(variable);
    }

    private static List<Class<?>> addPredefinedTypes(List<Class<?>> customTypes) {
        List<Class<?>> list = new ArrayList<>(customTypes.size() + 1);
        list.add(RuntimeType.class);
        list.addAll(customTypes);
        return Collections.unmodifiableList(list);
    }
}