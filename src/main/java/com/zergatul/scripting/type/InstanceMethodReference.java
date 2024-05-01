package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class InstanceMethodReference extends MethodReference {

    private final Class<?> owner;
    private final String name;
    private final SType returnType;
    private final MethodParameter[] parameters;

    public InstanceMethodReference(Class<?> owner, String name, SType returnType, MethodParameter... parameters) {
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
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
    public List<SType> getParameters() {
        return Arrays.stream(parameters).map(MethodParameter::type).toList();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(owner),
                name,
                Type.getMethodDescriptor(
                        Type.getType(returnType.getJavaClass()),
                        getParameters().stream().map(p -> Type.getType(p.getJavaClass())).toArray(Type[]::new)),
                false);
    }
}