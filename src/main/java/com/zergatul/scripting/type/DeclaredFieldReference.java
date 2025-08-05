package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

public class DeclaredFieldReference extends PropertyReference {

    private final SDeclaredType owner;
    private final SType type;
    private final String name;

    public DeclaredFieldReference(SDeclaredType owner, SType type, String name) {
        this.owner = owner;
        this.type = type;
        this.name = name;
    }

    @Override
    public SType getType() {
        return type;
    }

    @Override
    public boolean canGet() {
        return true;
    }

    @Override
    public boolean canSet() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void compileGet(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETFIELD, owner.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void compileSet(MethodVisitor visitor) {
        visitor.visitFieldInsn(PUTFIELD, owner.getInternalName(), name, type.getDescriptor());
    }
}