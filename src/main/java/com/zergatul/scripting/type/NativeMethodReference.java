package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class NativeMethodReference extends MethodReference {

    private final Method method;

    public NativeMethodReference(Method method) {
        this.method = method;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public SType getReturn() {
        return SType.fromJavaType(method.getReturnType());
    }

    @Override
    public List<SType> getParameters() {
        return Arrays.stream(method.getGenericParameterTypes())
                .map(SType::fromJavaType)
                .toList();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
    }
}