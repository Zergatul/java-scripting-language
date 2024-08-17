package com.zergatul.scripting.type;

import com.zergatul.scripting.runtime.MethodDescription;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Optional;

public abstract class MethodReference extends MemberReference {

    @SuppressWarnings("unused") // for monaco integration
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
}