package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class SMethodReferences extends SType {

    private final List<MethodReference> references;

    public SMethodReferences(List<MethodReference> references) {
        this.references = references;
    }

    public List<MethodReference> getReferences() {
        return this.references;
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