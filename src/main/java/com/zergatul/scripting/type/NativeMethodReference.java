package com.zergatul.scripting.type;

import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.MethodHandleCache;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

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
    public Optional<String> getDescription() {
        MethodDescription description = method.getAnnotation(MethodDescription.class);
        return description != null ? Optional.of(description.value()) : Optional.empty();
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }

    @Override
    public boolean isVirtual() {
        return !Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context, Runnable compileArguments) {
        if (isPublic()) {
            compileArguments.run();
            if (isStatic()) {
                visitor.visitMethodInsn(
                        INVOKESTATIC,
                        Type.getInternalName(method.getDeclaringClass()),
                        method.getName(),
                        Type.getMethodDescriptor(method),
                        false);
            } else {
                visitor.visitMethodInsn(
                        method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                        Type.getInternalName(method.getDeclaringClass()),
                        method.getName(),
                        Type.getMethodDescriptor(method),
                        method.getDeclaringClass().isInterface());
            }
        } else {
            if (isStatic()) {
                String methodHandleFieldName = context.createCachedPrivateMethodHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        methodHandleFieldName,
                        Type.getDescriptor(MethodHandle.class));
                compileArguments.run();
                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(MethodHandle.class),
                        "invokeExact",
                        getDescriptor(),
                        false);
            } else {
                String methodHandleFieldName = context.createCachedPrivateMethodHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        methodHandleFieldName,
                        Type.getDescriptor(MethodHandle.class));
                compileArguments.run();

                List<SType> types = getParameterTypes();
                Type[] argumentTypes = new Type[types.size() + 1];
                argumentTypes[0] = getOwner().getAsmType();
                for (int i = 1; i < argumentTypes.length; i++) {
                    argumentTypes[i] = types.get(i - 1).getAsmType();
                }

                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(MethodHandle.class),
                        "invokeExact",
                        Type.getMethodDescriptor(getReturn().getAsmType(), argumentTypes),
                        false);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NativeMethodReference other) {
            return other.method.equals(method);
        } else {
            return false;
        }
    }

    private boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }
}