package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class NativeMethodReference extends MethodReference {

    private final Method method;

    public NativeMethodReference(Method method) {
        this.method = method;
    }

    public Method getUnderlying() {
        return method;
    }

    @Override
    public SType getOwner() {
        return SType.fromJavaType(method.getDeclaringClass());
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public SType getReturn() {
        return SType.fromJavaType(method.getGenericReturnType());
    }

    @Override
    public List<MethodParameter> getParameters() {
        Parameter[] parameters = method.getParameters();
        java.lang.reflect.Type[] types = method.getGenericParameterTypes();
        List<MethodParameter> list = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            list.add(new MethodParameter(parameters[i].getName(), SType.fromJavaType(types[i])));
        }
        return list;
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