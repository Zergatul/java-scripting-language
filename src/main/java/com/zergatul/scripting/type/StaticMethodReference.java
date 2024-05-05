package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticMethodReference extends MethodReference {

    private final Class<?> ownerClass;
    private final String name;
    private final SType returnType;
    private final MethodParameter[] parameters;

    public StaticMethodReference(Class<?> ownerClass, String name, SType returnType, MethodParameter... parameters) {
        this.ownerClass = ownerClass;
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
                INVOKESTATIC,
                Type.getInternalName(ownerClass),
                name,
                Type.getMethodDescriptor(
                        Type.getType(returnType.getJavaClass()),
                        getParameters().stream()
                                .map(SType::getJavaClass)
                                .map(Type::getType)
                                .toArray(Type[]::new)),
                false);
    }
}
