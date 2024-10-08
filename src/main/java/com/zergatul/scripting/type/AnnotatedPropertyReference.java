package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class AnnotatedPropertyReference extends PropertyReference {

    private final String name;
    private final SType type;
    private final Method getter;
    private final Method setter;

    public AnnotatedPropertyReference(String name, SType type, Method getter, Method setter) {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public SType getType() {
        return type;
    }

    @Override
    public boolean canGet() {
        return getter != null;
    }

    @Override
    public boolean canSet() {
        return setter != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void compileGet(MethodVisitor visitor) {
        if (getter == null) {
            throw new InternalException();
        }

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getter.getDeclaringClass()),
                getter.getName(),
                Type.getMethodDescriptor(getter),
                false);
    }

    @Override
    public void compileSet(MethodVisitor visitor) {
        if (setter == null) {
            throw new InternalException();
        }

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(setter.getDeclaringClass()),
                setter.getName(),
                Type.getMethodDescriptor(setter),
                false);
    }
}