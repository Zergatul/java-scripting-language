package com.zergatul.scripting.type;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MethodReference extends MemberReference {

    private final Method method;

    public MethodReference(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public List<SType> getParameters() {
        return Arrays.stream(method.getParameterTypes())
                .map(SType::fromJavaClass)
                .toList();
    }

    public SType getReturn() {
        return SType.fromJavaClass(method.getReturnType());
    }
}