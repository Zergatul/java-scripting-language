package com.zergatul.scripting.type;

import com.zergatul.scripting.old.compiler.CompilerMethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SClassType extends SType {

    private final Class<?> clazz;

    public SClassType(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getJavaClass() {
        return this.clazz;
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        throw new RuntimeException();
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
}