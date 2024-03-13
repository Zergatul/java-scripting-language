package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.operation.EmptyUnaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;

public class SUnknown extends SType {

    public static final SUnknown instance = new SUnknown();

    private SUnknown() {

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
    public UnaryOperation implicitCastTo(SType other) {
        // allow SUnknown to be cast to anything
        // in this way any compilation error will not spread
        return EmptyUnaryOperation.instance;
    }
}