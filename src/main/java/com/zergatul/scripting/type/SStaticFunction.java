package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

public class SStaticFunction extends SFunction {

    public SStaticFunction(SType returnType, MethodParameter[] parameters) {
        super(returnType, parameters);
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
    public boolean equals(Object obj) {
        if (obj instanceof SStaticFunction other) {
            return other.matches(this);
        } else {
            return false;
        }
    }
}