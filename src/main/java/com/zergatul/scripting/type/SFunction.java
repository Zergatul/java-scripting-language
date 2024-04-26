package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;

public class SFunction extends SType {

    private final SType returnType;
    private final SType[] parameters;

    public SFunction(SType returnType, SType[] parameters) {
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public SType[] getParameters() {
        return parameters;
    }

    public SType getReturnType() {
        return returnType;
    }

    public String getDescriptor() {
        return Type.getMethodDescriptor(
                Type.getType(returnType.getJavaClass()),
                Arrays.stream(parameters).map(SType::getJavaClass).map(Type::getType).toArray(Type[]::new));
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
}