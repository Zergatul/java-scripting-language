package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

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
    public boolean canLoad() {
        return true;
    }

    @Override
    public boolean canStore() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void compileLoad(MethodVisitor visitor, CompilerContext context, Runnable compileCallee) {
        compileCallee.run();
        visitor.visitFieldInsn(GETFIELD, owner.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void compileStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileValue) {
        compileCallee.run();
        compileValue.run();
        visitor.visitFieldInsn(PUTFIELD, owner.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void compileLoadModifyStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileModify) {
        compileCallee.run();
        visitor.visitInsn(DUP);
        visitor.visitFieldInsn(GETFIELD, owner.getInternalName(), name, type.getDescriptor());
        compileModify.run();
        visitor.visitFieldInsn(PUTFIELD, owner.getInternalName(), name, type.getDescriptor());
    }

    public SDeclaredType getOwner() {
        return owner;
    }
}