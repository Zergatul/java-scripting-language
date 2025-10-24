package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class DeclaredMethodReference extends MethodReference {

    private final SDeclaredType owner;
    private final MemberModifiers modifiers;
    private final String name;
    private final SMethodFunction functionType;

    public DeclaredMethodReference(SDeclaredType owner, MemberModifiers modifiers, String name, SMethodFunction functionType) {
        this.owner = owner;
        this.modifiers = modifiers;
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
    public boolean isVirtual() {
        return modifiers.isVirtual();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                owner.getInternalName(),
                name,
                functionType.getMethodDescriptor(),
                false);
    }

    @Override
    public String getName() {
        return name;
    }
}