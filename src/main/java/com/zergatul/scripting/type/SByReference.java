package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SByReference extends SType {

    public static final SByReference BOOLEAN = new SByReference(SBoolean.instance);
    public static final SByReference INT8 = new SByReference(SInt8.instance);
    public static final SByReference INT16 = new SByReference(SInt16.instance);
    public static final SByReference INT = new SByReference(SInt.instance);
    public static final SByReference INT64 = new SByReference(SInt64.instance);
    public static final SByReference FLOAT32 = new SByReference(SFloat32.instance);
    public static final SByReference FLOAT = new SByReference(SFloat.instance);

    private final SType underlying;

    private SByReference(SType underlying) {
        this.underlying = underlying;
    }

    @Override
    public Class<?> getJavaClass() {
        return underlying.getReferenceClass();
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
    public boolean isSyntheticType() {
        return true;
    }

    @Override
    public String toString() {
        return "ref " + underlying.toString();
    }
}