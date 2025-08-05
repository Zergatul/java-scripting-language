package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class DeclaredMethodReference extends MethodReference {

    private final SDeclaredType owner;
    private final String name;
    private final SFunction functionType;

    public DeclaredMethodReference(SDeclaredType owner, String name, SFunction functionType) {
        this.owner = owner;
        this.name = name;
        this.functionType = functionType;
    }

    @Override
    public SType getOwner() {
        return owner;
    }

    @Override
    public SType getReturn() {
        return functionType.getReturnType();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return functionType.getParameters();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                owner.getInternalName(),
                name,
                functionType.getDescriptor(),
                false);
    }

    @Override
    public String getName() {
        return name;
    }
}