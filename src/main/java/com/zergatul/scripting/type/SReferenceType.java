package com.zergatul.scripting.type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.AASTORE;

public abstract class SReferenceType extends SType {

    @Override
    public boolean isReference() {
        return true;
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
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }
}