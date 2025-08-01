package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class NativeInstanceMethodReference extends NativeMethodReference {

    public NativeInstanceMethodReference(Method method) {
        super(method);
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                method.getDeclaringClass().isInterface());
    }
}