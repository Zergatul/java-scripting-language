package com.zergatul.scripting.type;

import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.PrivateMembersCache;
import com.zergatul.scripting.symbols.LocalVariable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
    public void compileInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments) {
        if (isStatic()) {
            compileInvoke(visitor, context, compileArguments, INVOKESTATIC);
        } else {
            int opcode = method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
            compileInvoke(visitor, context, compileArguments, opcode);
        }
    }

    @Override
    public void compileBaseInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments) {
        compileInvoke(visitor, context, compileArguments, INVOKESPECIAL);
    }

    @Override
    public void compileReflectionInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments) {
        String methodHandleFieldName = context.createCachedPrivateMethodMember(this);
        visitor.visitFieldInsn(
                GETSTATIC,
                PrivateMembersCache.INTERNAL_NAME,
                methodHandleFieldName,
                Type.getDescriptor(Method.class));

        if (isStatic()) {
            visitor.visitInsn(ACONST_NULL);
        }

        Class<?>[] parameters = method.getParameterTypes();
        visitor.visitLdcInsn(parameters.length);
        visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));

        context = context.createChild();
        LocalVariable parametersArrayVariable = new LocalVariable(new SArrayType(SJavaObject.instance));
        context.setStackIndex(parametersArrayVariable);
        parametersArrayVariable.compileStore(context, visitor);

        compileArguments.accept(context);
        for (int i = parameters.length - 1; i >= 0; i--) {
            SType paramType = SType.fromJavaType(parameters[i]);
            // ..., arg_i
            if (paramType instanceof SValueType) {
                SValueType valueType = (SValueType) paramType;
                valueType.compileBoxing(visitor);
            }
            parametersArrayVariable.compileLoad(context, visitor);
            // ..., arg_i, params
            visitor.visitInsn(SWAP);
            // ..., params, arg_i
            visitor.visitLdcInsn(i);
            // ..., params, arg_i, i
            visitor.visitInsn(SWAP);
            // ..., params, i, arg_i
            visitor.visitInsn(AASTORE);
        }

        parametersArrayVariable.compileLoad(context, visitor);
        // ..., Method, null, params

        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchStart = new Label();
        Label end = new Label();
        visitor.visitTryCatchBlock(tryStart, tryEnd, catchStart, Type.getInternalName(InvocationTargetException.class));

        visitor.visitLabel(tryStart);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Method.class),
                "invoke",
                Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.getType(Object[].class)),
                false);
        visitor.visitLabel(tryEnd);
        visitor.visitJumpInsn(GOTO, end);

        // restore original exception
        visitor.visitLabel(catchStart);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(InvocationTargetException.class),
                "getCause",
                Type.getMethodDescriptor(Type.getType(Throwable.class)),
                false);
        visitor.visitInsn(ATHROW);

        visitor.visitLabel(end);

        if (getReturn() == SVoidType.instance) {
            visitor.visitInsn(POP);
        } else {
            if (getReturn() instanceof SValueType) {
                SValueType valueType = (SValueType) getReturn();
                visitor.visitTypeInsn(CHECKCAST, valueType.getBoxed().getInternalName());
                valueType.compileUnboxing(visitor);
            } else {
                if (getReturn() != SJavaObject.instance) {
                    visitor.visitTypeInsn(CHECKCAST, getReturn().getInternalName());
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NativeMethodReference) {
            NativeMethodReference other = (NativeMethodReference) obj;
            return other.method.equals(method);
        } else {
            return false;
        }
    }

    private void compileInvoke(MethodVisitor visitor, CompilerContext context, Consumer<CompilerContext> compileArguments, int opcode) {
        compileArguments.accept(context);
        visitor.visitMethodInsn(
                opcode,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                method.getDeclaringClass().isInterface());
    }
}