package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.util.concurrent.CompletableFuture;

public class SFuture extends SType {

    private final SType underlying;

    public SFuture(SType underlying) {
        this.underlying = underlying;
    }

    public SType getUnderlying() {
        return this.underlying;
    }

    @Override
    public Class<?> getJavaClass() {
        return CompletableFuture.class;
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
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
        return true;
    }

    @Override
    public int getReturnInst() {
        throw new InternalException();
    }

    @Override
    public String toString() {
        return String.format("Future<%s>", underlying);
    }
}
