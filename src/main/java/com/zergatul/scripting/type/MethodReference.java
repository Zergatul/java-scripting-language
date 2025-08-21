package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Optional;

public abstract class MethodReference extends MemberReference implements Invocable {

    public abstract SType getOwner();

    public abstract SType getReturn();

    public abstract List<MethodParameter> getParameters();

    public Optional<String> getDescription() {
        return Optional.empty();
    }

    public abstract void compileInvoke(MethodVisitor visitor);

    public List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }

    public String getDescriptor() {
        return Type.getMethodDescriptor(
                getReturn().getAsmType(),
                getParameterTypes().stream().map(SType::getAsmType).toArray(Type[]::new));
    }

    public boolean matches(SFunction other) {
        List<MethodParameter> parameters1 = getParameters();
        List<MethodParameter> parameters2 = other.getParameters();
        if (parameters1.size() != parameters2.size()) {
            return false;
        }
        if (!this.getReturn().equals(other.getReturnType())) {
            return false;
        }
        for (int i = 0; i < parameters1.size(); i++) {
            if (!parameters1.get(i).type().equals(parameters2.get(i).type())) {
                return false;
            }
        }
        return true;
    }
}