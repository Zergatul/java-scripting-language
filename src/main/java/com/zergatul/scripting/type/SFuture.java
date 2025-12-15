package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SFuture extends SReferenceType {

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
    public List<MethodReference> getInstanceMethods() {
        try {
            Method method = CompletableFuture.class.getMethod("isDone");
            return List.of(new NativeInstanceMethodReference(method));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalException();
        }
    }

    @Override
    public String toString() {
        return String.format("Future<%s>", underlying);
    }
}