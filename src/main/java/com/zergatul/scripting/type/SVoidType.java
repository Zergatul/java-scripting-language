package com.zergatul.scripting.type;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

public class SVoidType extends SType {

    public static final SVoidType instance = new SVoidType();

    private SVoidType() {}

    @Override
    public boolean isPredefined() {
        return true;
    }

    @Override
    public Class<?> getJavaClass() {
        return void.class;
    }

    @Override
    public @Nullable SType getBaseType() {
        return null;
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
    }

    @Override
    public boolean isReference() {
        throw new IllegalStateException();
    }

    @Override
    public boolean hasDefaultValue() {
        throw new IllegalStateException();
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
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