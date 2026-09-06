package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.utility.Lists;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticMethodReference extends MethodReference {

    private final Class<?> ownerClass;
    private final SType ownerType;
    private final String name;
    private final SType returnType;
    private final MethodParameter[] parameters;

    public StaticMethodReference(Class<?> ownerClass, SType ownerType, String name, SType returnType, MethodParameter... parameters) {
        this.ownerClass = ownerClass;
        this.ownerType = ownerType;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public SType getOwner() {
        return ownerType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SType getReturn() {
        return returnType;
    }

    @Override
    public List<MethodParameter> getParameters() {
        return Lists.of(parameters);
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments) {
        compileArguments.accept(context);
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(ownerClass),
                name,
                getDescriptor(),
                false);
    }
}