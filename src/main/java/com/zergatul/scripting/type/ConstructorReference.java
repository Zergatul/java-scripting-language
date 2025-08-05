package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Optional;

public abstract class ConstructorReference {

    public Optional<String> getDescription() {
        return Optional.empty();
    }

    public abstract void compileInvoke(MethodVisitor visitor);
    public abstract List<MethodParameter> getParameters();

    public List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }
}