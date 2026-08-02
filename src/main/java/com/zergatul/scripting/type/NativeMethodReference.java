package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.MethodHandleCache;
import org.jspecify.annotations.Nullable;
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

    private final SType owner;
    private final Method method;
    private final @Nullable GenericSubstitution substitution;

    public NativeMethodReference(Method method) {
        this(SType.fromJavaType(method.getDeclaringClass()), method);
    }

    public NativeMethodReference(SType owner, Method method) {
        this.owner = owner;
        this.method = method;
        this.substitution = null;
    }

    public NativeMethodReference(SType owner, Method method, GenericSubstitution substitution) {
        this.owner = owner;
        this.method = method;
        this.substitution = substitution;
    }

    public Method getUnderlying() {
        return method;
    }

    @Override
    public SType getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public SType getReturn() {
        if (substitution == null) {
            return SType.fromJavaType(method.getGenericReturnType());
        } else {
            return substitution.resolveType(method.getGenericReturnType());
        }
    }

    @Override
    public List<MethodParameter> getParameters() {
        Parameter[] parameters = method.getParameters();
        java.lang.reflect.Type[] types = method.getGenericParameterTypes();
        List<MethodParameter> list = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            SType type;
            if (substitution == null) {
                type = SType.fromJavaType(types[i]);
            } else {
                type = substitution.resolveType(types[i]);
            }
            list.add(new MethodParameter(parameters[i].getName(), type));
        }
        return list;
    }

    @Override
    public Optional<String> getDescription() {
        MethodDescription description = method.getAnnotation(MethodDescription.class);
        return description != null ? Optional.of(description.value()) : Optional.empty();
    }

    @Override
    public Visibility getVisibility() {
        if (Modifier.isPublic(method.getModifiers())) {
            return Visibility.PUBLIC;
        } else if (Modifier.isProtected(method.getModifiers())) {
            return Visibility.PROTECTED;
        } else {
            return Visibility.PRIVATE;
        }
    }

    @Override
    public boolean isVirtual() {
        return !Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context, Runnable compileArguments) {
        if (isStatic()) {
            compileInvoke(visitor, compileArguments, INVOKESTATIC);
        } else {
            int opcode = method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
            compileInvoke(visitor, compileArguments, opcode);
        }
    }

    @Override
    public void compileBaseInvoke(MethodVisitor visitor, CompilerContext context, Runnable compileArguments) {
        compileInvoke(visitor, compileArguments, INVOKESPECIAL);
    }

    @Override
    public void compileMethodHandleInvoke(MethodVisitor visitor, CompilerContext context, Runnable compileArguments) {
        String methodHandleFieldName = context.createCachedPrivateMethodHandle(this);
        visitor.visitFieldInsn(
                GETSTATIC,
                MethodHandleCache.INTERNAL_NAME,
                methodHandleFieldName,
                Type.getDescriptor(MethodHandle.class));
        compileArguments.run();

        if (isStatic()) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(MethodHandle.class),
                    "invokeExact",
                    getDescriptor(),
                    false);
        } else {
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

    @Override
    public void compileReturnBridge(MethodVisitor visitor) {
        if (substitution == null) {
            return;
        }

        java.lang.reflect.Type actualType = method.getGenericReturnType();
        Class<?> expectedType = method.getReturnType();
        if (actualType.equals(expectedType)) {
            return;
        }

        SType actual = substitution.resolveType(actualType);
        SType expected = SType.fromJavaType(expectedType);

        if (actual instanceof SValueType valueType && expected.isReference()) {
            if (!valueType.getBoxed().isAssignableFrom(expected)) {
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(valueType.getBoxed().getJavaClass()));
            }
            valueType.compileUnboxing(visitor);
        } else {
            if (!actual.isAssignableFrom(expected)) {
                visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(actual.getJavaClass()));
            }
        }
    }

    @Override
    public void compileArgumentBridge(MethodVisitor visitor, int index) {
        if (substitution == null) {
            return;
        }

        java.lang.reflect.Type actualType = method.getGenericParameterTypes()[index];
        Class<?> expectedType = method.getParameterTypes()[index];
        if (actualType.equals(expectedType)) {
            return;
        }

        SType actual = substitution.resolveType(actualType);
        SType expected = SType.fromJavaType(expectedType);

        if (actual instanceof SValueType valueType && expected.isReference()) {
            valueType.compileBoxing(visitor);
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

    private void compileInvoke(MethodVisitor visitor, Runnable compileArguments, int opcode) {
        compileArguments.run();
        visitor.visitMethodInsn(
                opcode,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                method.getDeclaringClass().isInterface());
    }
}