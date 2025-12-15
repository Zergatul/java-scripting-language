package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.BinaryOperatorMethod;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class OverloadBinaryOperation extends BinaryOperation {

    private final Method method;

    private OverloadBinaryOperation(Method method, BinaryOperator operator, SType type, SType left, SType right) {
        super(operator, type, left, right);
        this.method = method;
    }

    @Override
    public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
        right.release(left);
        left.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
    }

    public static @Nullable OverloadBinaryOperation fromMethod(SType type, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        if (!Modifier.isStatic(method.getModifiers())) {
            return null;
        }

        BinaryOperatorMethod annotation = method.getAnnotation(BinaryOperatorMethod.class);
        if (annotation == null) {
            return null;
        }

        if (method.getReturnType().equals(void.class)) {
            return null;
        }

        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 2) {
            return null;
        }

        boolean leftMatch = parameters[0].equals(type.getJavaClass());
        boolean rightMatch = parameters[1].equals(type.getJavaClass());

        if (!leftMatch && !rightMatch) {
            return null;
        }

        return new OverloadBinaryOperation(
                method,
                annotation.value(),
                SType.fromJavaType(method.getReturnType()),
                leftMatch ? type : SType.fromJavaType(parameters[0]),
                rightMatch ? type : SType.fromJavaType(parameters[1]));
    }
}