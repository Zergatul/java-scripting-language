package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class InstanceMethodBasedPropertyReference extends PropertyReference {

    private final String name;
    private final Method getMethod;
    private final SType type;

    public InstanceMethodBasedPropertyReference(String name, Class<?> clazz, String getMethodName) {
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
    public boolean canLoad() {
        return true;
    }

    @Override
    public boolean canStore() {
        return false;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor, Runnable compileCallee) {
        compileCallee.run();
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getMethod.getDeclaringClass()),
                getMethod.getName(),
                Type.getMethodDescriptor(getMethod),
                false);
    }
}