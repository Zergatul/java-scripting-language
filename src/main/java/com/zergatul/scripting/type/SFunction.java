package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

public abstract class SFunction extends SReferenceType {

    private final SType returnType;
    private final MethodParameter[] parameters;

    protected SFunction(SType returnType, MethodParameter[] parameters) {
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public List<MethodParameter> getParameters() {
        return List.of(parameters);
    }

    public List<SType> getParameterTypes() {
        return Arrays.stream(parameters).map(MethodParameter::type).toList();
    }

    public SType getReturnType() {
        return returnType;
    }

    public boolean isFunction() {
        return returnType != SVoidType.instance;
    }

    public String getMethodDescriptor() {
        return Type.getMethodDescriptor(
                Type.getType(returnType.getJavaClass()),
                getParameterTypes().stream().map(SType::getDescriptor).map(Type::getType).toArray(Type[]::new));
    }

    public boolean matches(SFunction other) {
        if (this.parameters.length != other.parameters.length) {
            return false;
        }
        if (!this.returnType.equals(other.returnType)) {
            return false;
        }
        for (int i = 0; i < this.parameters.length; i++) {
            if (!this.parameters[i].type().equals(other.parameters[i].type())) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(SType returnType, SType[] parameters) {
        if (this.parameters.length != parameters.length) {
            return false;
        }
        if (!this.returnType.equals(returnType)) {
            return false;
        }
        for (int i = 0; i < this.parameters.length; i++) {
            if (!this.parameters[i].type().equals(parameters[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("fn<");
        List<SType> parameters = getParameterTypes();
        if (parameters.size() != 1) {
            sb.append('(');
        }
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(parameters.get(i));
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        if (parameters.size() != 1) {
            sb.append(')');
        }
        sb.append(" => ");
        sb.append(returnType.toString());
        sb.append('>');
        return sb.toString();
    }
}