package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.runtime.FloatUtils;
import com.zergatul.scripting.type.operation.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SFloat extends SPredefinedType {

    public static final SFloat instance = new SFloat();
    public static final SStaticTypeReference staticRef = new SStaticTypeReference(instance);

    private SFloat() {
        super(double.class);
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public int getLoadInst() {
        return DLOAD;
    }

    @Override
    public int getStoreInst() {
        return DSTORE;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitLdcInsn(0.0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_DOUBLE;
    }

    @Override
    public int getArrayLoadInst() {
        return DALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return DASTORE;
    }

    @Override
    public boolean isJvmCategoryOneComputationalType() {
        return false;
    }

    @Override
    public BinaryOperation add(SType other) {
        return other == this ? ADD : null;
    }

    @Override
    public BinaryOperation subtract(SType other) {
        return other == this ? SUB : null;
    }

    @Override
    public BinaryOperation multiply(SType other) {
        return other == this ? MUL : null;
    }

    @Override
    public BinaryOperation divide(SType other) {
        return other == this ? DIV : null;
    }

    @Override
    public BinaryOperation modulo(SType other) {
        return other == this ? MOD : null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == this ? LESS_THAN : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == this ? GREATER_THAN : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == this ? LESS_THAN_EQUALS : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == this ? GREATER_THAN_EQUALS : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == this ? EQUALS : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == this ? NOT_EQUALS : null;
    }

    @Override
    public UnaryOperation plus() {
        return PLUS;
    }

    @Override
    public UnaryOperation minus() {
        return MINUS;
    }

    @Override
    public int getReturnInst() {
        return DRETURN;
    }

    @Override
    public Class<?> getBoxedVersion() {
        return Double.class;
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Double.class),
                "doubleValue",
                Type.getMethodDescriptor(Type.DOUBLE_TYPE),
                false);
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(METHOD_TO_STRING, METHOD_TO_STANDARD_STRING);
    }

    @Override
    public String toString() {
        return "float";
    }

    private static final BinaryOperation ADD = new SingleInstructionBinaryOperation(SFloat.instance, DADD);
    private static final BinaryOperation SUB = new SingleInstructionBinaryOperation(SFloat.instance, DSUB);
    private static final BinaryOperation MUL = new SingleInstructionBinaryOperation(SFloat.instance, DMUL);
    private static final BinaryOperation DIV = new SingleInstructionBinaryOperation(SFloat.instance, DDIV);
    private static final BinaryOperation MOD = new SingleInstructionBinaryOperation(SFloat.instance, DREM);
    private static final BinaryOperation LESS_THAN = new FloatComparisonOperation(IF_ICMPLT);
    private static final BinaryOperation GREATER_THAN = new FloatComparisonOperation(IF_ICMPGT);
    private static final BinaryOperation LESS_THAN_EQUALS = new FloatComparisonOperation(IF_ICMPLE);
    private static final BinaryOperation GREATER_THAN_EQUALS = new FloatComparisonOperation(IF_ICMPGE);
    private static final BinaryOperation EQUALS = new FloatComparisonOperation(IF_ICMPEQ);
    private static final BinaryOperation NOT_EQUALS = new FloatComparisonOperation(IF_ICMPNE);
    public static final UnaryOperation PLUS = new UnaryOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };
    public static final UnaryOperation MINUS = new UnaryOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(DNEG);
        }
    };

    private static final MethodReference METHOD_TO_STRING = new StaticMethodReference(
            Double.class,
            SFloat.instance,
            "toString",
            SString.instance);

    private static final MethodReference METHOD_TO_STANDARD_STRING = new StaticMethodReference(
            FloatUtils.class,
            SFloat.instance,
            "toStandardString",
            SString.instance,
            new MethodParameter("digits", SInt.instance));

    private static class FloatComparisonOperation extends BinaryOperation {

        private final int opcode;

        protected FloatComparisonOperation(int opcode) {
            super(SBoolean.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitInsn(DCMPG);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }
}