package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MethodBasedPropertyReference extends PropertyReference {

    private final String name;
    private final Method getMethod;
    private final SType type;

    public MethodBasedPropertyReference(String name, Class<?> clazz, String getMethodName) {
        this.name = name;
        try {
            getMethod = clazz.getDeclaredMethod(getMethodName);
        } catch (NoSuchMethodException e) {
            throw new InternalException();
        }
        type = SType.fromJavaType(getMethod.getReturnType());
    }

    @Override
    public String getName() {
        return name;
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
        return false;
    }

    @Override
    public void compileGet(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getMethod.getDeclaringClass()),
                getMethod.getName(),
                Type.getMethodDescriptor(getMethod),
                false);
    }
}