package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SAction extends SType {

    public static final SAction instance = new SAction();

    private SAction() {

    }

    @Override
    public Class<?> getJavaClass() {
        return Runnable.class;
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
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public String toString() {
        return "Action";
    }
}