package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class InstanceMethodReference extends MethodReference {

    private final Class<?> ownerClass;
    private final SType ownerType;
    private final String name;
    private final SType returnType;
    private final MethodParameter[] parameters;

    public InstanceMethodReference(Class<?> ownerClass, SType ownerType, String name, SType returnType, MethodParameter... parameters) {
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
        return List.of(parameters);
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(ownerClass),
                name,
                getDescriptor(),
                false);
    }
}