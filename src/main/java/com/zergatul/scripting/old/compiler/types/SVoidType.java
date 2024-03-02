package com.zergatul.scripting.old.compiler.types;

import com.zergatul.scripting.old.compiler.CompilerMethodVisitor;

import static org.objectweb.asm.Opcodes.RETURN;

public class SVoidType extends SPrimitiveType {

    public static final SVoidType instance = new SVoidType();

    private SVoidType() {
        super(void.class);
    }

    @Override
    public boolean isReference() {
        throw new IllegalStateException();
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        throw new IllegalStateException();
    }

    @Override
    public int getLoadInst() {
        throw new IllegalStateException();
    }

    @Override
    public int getStoreInst() {
        throw new IllegalStateException();
    }

    @Override
    public int getArrayTypeInst() {
        throw new IllegalStateException();
    }

    @Override
    public int getArrayLoadInst() {
        throw new IllegalStateException();
    }

    @Override
    public int getArrayStoreInst() {
        throw new IllegalStateException();
    }

    @Override
    public int getReturnInst() {
        return RETURN;
    }

    @Override
    public String toString() {
        return "void";
    }
}
