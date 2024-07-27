package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class SStaticTypeReference extends SType {

    private final SType underlying;

    public SStaticTypeReference(SType underlying) {
        this.underlying = underlying;
    }

    public SType getUnderlying() {
        return underlying;
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
    public List<MethodReference> getInstanceMethods() {
        return underlying.getStaticMethods();
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return underlying.getStaticProperties();
    }
}