package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.SBoolean;
import com.zergatul.scripting.type.SStringType;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class StringOperations {
    public static final BinaryOperation CONCAT = new StringConcatOperation();
    /*public static final BinaryOperation LT = new IntComparisonOperation(IF_ICMPLT);
    public static final BinaryOperation GT = new IntComparisonOperation(IF_ICMPGT);
    public static final BinaryOperation LTE = new IntComparisonOperation(IF_ICMPLE);
    public static final BinaryOperation GTE = new IntComparisonOperation(IF_ICMPGE);*/
    public static final BinaryOperation EQ = new StringEqualsOperation();
    public static final BinaryOperation NEQ = new StringNotEqualsOperation();

    private static class StringConcatOperation extends BinaryOperation {

        private final Constructor<StringBuilder> constructor;
        private final Method appendMethod;
        private final Method toStringMethod;

        protected StringConcatOperation() {
            super(SStringType.instance);

            try {
                constructor = StringBuilder.class.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new InternalException();
            }

            try {
                appendMethod = StringBuilder.class.getDeclaredMethod("append", String.class);
            } catch (NoSuchMethodException e) {
                throw new InternalException();
            }

            try {
                toStringMethod = StringBuilder.class.getDeclaredMethod("toString");
            } catch (NoSuchMethodException e) {
                throw new InternalException();
            }
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            // stack = ..., left
            left.visitTypeInsn(NEW, Type.getInternalName(StringBuilder.class));
            // stack = ..., left, builder
            left.visitInsn(DUP);
            // stack = ..., left, builder, builder
            left.visitMethodInsn(
                    INVOKESPECIAL,
                    Type.getInternalName(StringBuilder.class),
                    "<init>",
                    Type.getConstructorDescriptor(constructor),
                    false);
            // stack = ..., left, builder
            left.visitInsn(SWAP);
            // stack = ..., builder, left
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    appendMethod.getName(),
                    Type.getMethodDescriptor(appendMethod),
                    false);
            // stack = ..., builder
            right.release(left);
            // stack = ..., builder, left
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    appendMethod.getName(),
                    Type.getMethodDescriptor(appendMethod),
                    false);
            // stack = ..., builder
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    toStringMethod.getName(),
                    Type.getMethodDescriptor(toStringMethod),
                    false);
        }
    }

    private static class StringEqualsOperation extends BinaryOperation {

        private final Method equalsMethod;

        protected StringEqualsOperation() {
            super(SBoolean.instance);

            try {
                equalsMethod = Objects.class.getDeclaredMethod("equals", Object.class, Object.class);
            } catch (NoSuchMethodException e) {
                throw new InternalException();
            }
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Objects.class),
                    equalsMethod.getName(),
                    Type.getMethodDescriptor(equalsMethod),
                    false);
        }
    }

    private static class StringNotEqualsOperation extends BinaryOperation {

        public StringNotEqualsOperation() {
            super(SBoolean.instance);
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            EQ.apply(left, right);
            BooleanOperations.NOT.apply(left);
        }
    }
}