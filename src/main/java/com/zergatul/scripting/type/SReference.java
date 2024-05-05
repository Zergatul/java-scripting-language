package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;

public class SReference extends SType {

    public static final SReference INT = new SReference(SInt.instance);
    public static final SReference FLOAT = new SReference(SFloat.instance);

    private final SType underlying;

    private SReference(SType underlying) {
        this.underlying = underlying;
    }

    @Override
    public Class<?> getJavaClass() {
        return underlying.getReferenceClass();
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
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
    public String toString() {
        return "ref " + underlying.toString();
    }
}