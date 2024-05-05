package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.runtime.IntReference;
import com.zergatul.scripting.runtime.IntUtils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.SingleInstructionBinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SInt extends SPredefinedType {

    public static final SInt instance = new SInt();
    public static final SStaticTypeReference staticRef = new SStaticTypeReference(instance);

    private SInt() {
        super(int.class);
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public int getLoadInst() {
        return ILOAD;
    }

    @Override
    public int getStoreInst() {
        return ISTORE;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitInsn(ICONST_0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_INT;
    }

    @Override
    public int getArrayLoadInst() {
        return IALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return IASTORE;
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
    public BinaryOperation bitwiseAnd(SType other) {
        return other == this ? BITWISE_AND : null;
    }

    @Override
    public BinaryOperation bitwiseOr(SType other) {
        return other == this ? BITWISE_OR : null;
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
    public UnaryOperation increment() {
        return INC;
    }

    @Override
    public UnaryOperation decrement() {
        return DEC;
    }

    @Override
    public UnaryOperation implicitCastTo(SType other) {
        return other == SFloat.instance ? TO_FLOAT : null;
    }

    @Override
    public int getReturnInst() {
        return IRETURN;
    }

    @Override
    public Class<?> getBoxedVersion() {
        return Integer.class;
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Integer.class),
                "intValue",
                Type.getMethodDescriptor(Type.INT_TYPE),
                false);
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(METHOD_TO_STRING, METHOD_TO_STANDARD_STRING);
    }

    @Override
    public List<MethodReference> getStaticMethods() {
        return List.of(METHOD_TRY_PARSE);
    }

    @Override
    public SReference getReferenceType() {
        return SReference.INT;
    }

    @Override
    public Class<?> getReferenceClass() {
        return IntReference.class;
    }

    @Override
    public String toString() {
        return "int";
    }

    private static final BinaryOperation ADD = new SingleInstructionBinaryOperation(SInt.instance, IADD);
    private static final BinaryOperation SUB = new SingleInstructionBinaryOperation(SInt.instance, ISUB);
    private static final BinaryOperation MUL = new SingleInstructionBinaryOperation(SInt.instance, IMUL);
    private static final BinaryOperation DIV = new SingleInstructionBinaryOperation(SInt.instance, IDIV);
    private static final BinaryOperation MOD = new SingleInstructionBinaryOperation(SInt.instance, IREM);
    private static final BinaryOperation BITWISE_AND = new SingleInstructionBinaryOperation(SInt.instance, IAND);
    private static final BinaryOperation BITWISE_OR = new SingleInstructionBinaryOperation(SInt.instance, IOR);
    private static final BinaryOperation LESS_THAN = new IntComparisonOperation(IF_ICMPLT);
    private static final BinaryOperation GREATER_THAN = new IntComparisonOperation(IF_ICMPGT);
    private static final BinaryOperation LESS_THAN_EQUALS = new IntComparisonOperation(IF_ICMPLE);
    private static final BinaryOperation GREATER_THAN_EQUALS = new IntComparisonOperation(IF_ICMPGE);
    private static final BinaryOperation EQUALS = new IntComparisonOperation(IF_ICMPEQ);
    private static final BinaryOperation NOT_EQUALS = new IntComparisonOperation(IF_ICMPNE);
    private static final UnaryOperation PLUS = new UnaryOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };
    private static final UnaryOperation MINUS = new UnaryOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(INEG);
        }
    };
    private static final UnaryOperation INC = new UnaryOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(ICONST_1);
            visitor.visitInsn(IADD);
        }
    };
    private static final UnaryOperation DEC = new UnaryOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(ICONST_1);
            visitor.visitInsn(ISUB);
        }
    };
    private static final UnaryOperation TO_FLOAT = new UnaryOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(I2D);
        }
    };

    private static final MethodReference METHOD_TO_STRING = new StaticAsInstanceMethodReference(
            Integer.class,
            SInt.instance,
            "toString",
            SString.instance);

    private static final MethodReference METHOD_TO_STANDARD_STRING = new StaticAsInstanceMethodReference(
            IntUtils.class,
            SInt.instance,
            "toStandardString",
            SString.instance);

    private static final MethodReference METHOD_TRY_PARSE = new StaticMethodReference(
            IntUtils.class,
            "tryParse",
            SBoolean.instance,
            new MethodParameter("str", SString.instance),
            new MethodParameter("result", SReference.INT));

    private static class IntComparisonOperation extends BinaryOperation {

        private final int opcode;

        public IntComparisonOperation(int opcode) {
            super(SBoolean.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }
}