package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.operation.CastOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

public class SFunction extends SType {

    private final SType returnType;
    private final MethodParameter[] parameters;

    public SFunction(SType returnType, MethodParameter[] parameters) {
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

    public String getDescriptor() {
        return Type.getMethodDescriptor(
                Type.getType(returnType.getJavaClass()),
                getParameterTypes().stream().map(SType::getJavaClass).map(Type::getType).toArray(Type[]::new));
    }

    @Override
    public boolean isSyntheticType() {
        return true;
    }

    @Override
    public Class<?> getJavaClass() {
        throw new InternalException();
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public int getLoadInst() {
        throw new InternalException();
    }

    @Override
    public int getStoreInst() {
        throw new InternalException();
    }

    @Override
    public int getArrayLoadInst() {
        throw new InternalException();
    }

    @Override
    public int getArrayStoreInst() {
        throw new InternalException();
    }

    @Override
    public boolean isReference() {
        throw new InternalException();
    }

    @Override
    public int getReturnInst() {
        throw new InternalException();
    }

    @Override
    public CastOperation implicitCastTo(SType other) {
        if (other instanceof SFunctionalInterface lambdaType) {
            if (parametersMatch(lambdaType)) {
                return new FunctionToLambdaOperation(other);
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SFunction other) {
            return other.returnType.equals(returnType) && Arrays.equals(other.parameters, parameters);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (SType type : getParameterTypes()) {
            sb.append(type.toString()).append(", ");
        }
        if (parameters.length > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append(") => ");
        sb.append(returnType.toString());
        return sb.toString();
    }

    private boolean parametersMatch(SFunctionalInterface lambda) {
        if (!returnType.equals(lambda.getActualReturnType())) {
            return false;
        }
        if (parameters.length != lambda.getActualParameters().length) {
            return false;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].type().equals(lambda.getActualParameters()[i])) {
                return false;
            }
        }
        return true;
    }

    public static class FunctionToLambdaOperation extends CastOperation {

        private FunctionToLambdaOperation(SType type) {
            super(type);
        }

        @Override
        public void apply(MethodVisitor visitor) {}
    }
}