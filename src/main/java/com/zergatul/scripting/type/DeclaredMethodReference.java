package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
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
    public boolean isAbstract() {
        return modifiers.isAbstract();
    }

    @Override
    public boolean isFinal() {
        return modifiers.isFinal();
    }

    @Override
    public Visibility getVisibility() {
        return modifiers.getVisibility();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments) {
        compileInvoke(visitor, context, compileArguments, INVOKEVIRTUAL);
    }

    @Override
    public void compileBaseInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments) {
        compileInvoke(visitor, context, compileArguments, INVOKESPECIAL);
    }

    @Override
    public String getName() {
        return name;
    }

    private void compileInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments, int opcode) {
        compileArguments.accept(context);
        visitor.visitMethodInsn(
                opcode,
                owner.getInternalName(),
                name,
                functionType.getMethodDescriptor(),
                false);
    }
}