package com.zergatul.scripting.type;

import com.zergatul.scripting.MethodDescription;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class NativeMethodReference extends MethodReference {

    protected final Method method;

    protected NativeMethodReference(Method method) {
        this.method = method;
    }

    public Method getUnderlying() {
        return method;
    }

    @Override
    public SType getOwner() {
        return SType.fromJavaType(method.getDeclaringClass());
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public SType getReturn() {
        return SType.fromJavaType(method.getGenericReturnType());
    }

    @Override
    public List<MethodParameter> getParameters() {
        Parameter[] parameters = method.getParameters();
        java.lang.reflect.Type[] types = method.getGenericParameterTypes();
        List<MethodParameter> list = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            list.add(new MethodParameter(parameters[i].getName(), SType.fromJavaType(types[i])));
        }
        return list;
    }

    @Override
    public Optional<String> getDescription() {
        MethodDescription description = method.getAnnotation(MethodDescription.class);
        return description != null ? Optional.of(description.value()) : Optional.empty();
    }

    @Override
    public boolean isVirtual() {
        return !Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NativeMethodReference other) {
            return other.method.equals(method);
        } else {
            return false;
        }
    }
}