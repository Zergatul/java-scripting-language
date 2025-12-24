package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.UnaryOperatorMethod;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.DeclaredUnaryOperationReference;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class OverloadUnaryOperation extends UnaryOperation {

    private final SType owner;
    private final String methodName;
    private final Supplier<String> methodDescriptor;

    public OverloadUnaryOperation(DeclaredUnaryOperationReference operationRef) {
        super(operationRef.getOperator(), operationRef.getReturn(), operationRef.getParameters().getFirst().type());
        this.owner = operationRef.getOwner();
        this.methodName = operationRef.getName();
        this.methodDescriptor = operationRef::getDescriptor;
    }

    private OverloadUnaryOperation(SType owner, Method method, UnaryOperator operator, SType resultType) {
        super(operator, resultType, owner);
        this.owner = owner;
        this.methodName = method.getName();
        this.methodDescriptor = () -> Type.getMethodDescriptor(method);
    }

    @Override
    public void apply(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                owner.getInternalName(),
                methodName,
                methodDescriptor.get(),
                false);
    }

    public static @Nullable OverloadUnaryOperation fromMethod(SType type, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        if (!Modifier.isStatic(method.getModifiers())) {
            return null;
        }

        UnaryOperatorMethod annotation = method.getAnnotation(UnaryOperatorMethod.class);
        if (annotation == null) {
            return null;
        }

        if (method.getReturnType().equals(void.class)) {
            return null;
        }

        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 1) {
            return null;
        }

        if (!parameters[0].equals(type.getJavaClass())) {
            return null;
        }

        return new OverloadUnaryOperation(
                type,
                method,
                annotation.value(),
                SType.fromJavaType(method.getReturnType()));
    }
}