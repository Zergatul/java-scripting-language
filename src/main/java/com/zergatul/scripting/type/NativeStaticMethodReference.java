package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class NativeStaticMethodReference extends NativeMethodReference {

    public NativeStaticMethodReference(Method method) {
        super(method);
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
    }
}