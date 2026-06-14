package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Optional;

public abstract class ConstructorReference implements Invocable {

    public Optional<String> getDescription() {
        return Optional.empty();
    }

    public abstract SType getOwner();

    public abstract void compileInvoke(MethodVisitor visitor);
    public abstract List<MethodParameter> getParameters();

    @Override
    public List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }

    @Override
    public String toDiagnosticsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("constructor ");
        sb.append(getOwner());
        sb.append('(');
        List<MethodParameter> parameters = getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            MethodParameter parameter = parameters.get(i);
            sb.append(parameter.type());
            sb.append(' ');
            sb.append(parameter.name());
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }
}