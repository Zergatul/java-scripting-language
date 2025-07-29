package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.BooleanReference;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import com.zergatul.scripting.type.operation.SingleInstructionBinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SBoolean extends SPredefinedType {

    public static final SBoolean instance = new SBoolean();

    private SBoolean() {
        super(boolean.class);
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
        return T_BOOLEAN;
    }

    @Override
    public int getArrayLoadInst() {
        return BALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return BASTORE;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == this ? SInt.instance.lessThan(SInt.instance) : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == this ? SInt.instance.greaterThan(SInt.instance) : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == this ? SInt.instance.lessEquals(SInt.instance) : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == this ? SInt.instance.greaterEquals(SInt.instance) : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == this ? SInt.instance.equalsOp(SInt.instance) : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == this ? SInt.instance.notEqualsOp(SInt.instance) : null;
    }

    @Override
    public BinaryOperation booleanAnd(SType other) {
        return other == this ? BOOLEAN_AND.value() : null;
    }

    @Override
    public BinaryOperation booleanOr(SType other) {
        return other == this ? BOOLEAN_OR.value() : null;
    }

    @Override
    public BinaryOperation bitwiseAnd(SType other) {
        return other == this ? BITWISE_AND.value() : null;
    }

    @Override
    public BinaryOperation bitwiseOr(SType other) {
        return other == this ? BITWISE_OR.value() : null;
    }

    @Override
    public UnaryOperation not() {
        return NOT.value();
    }

    @Override
    public CastOperation implicitCastTo(SType other) {
        if (other instanceof SClassType && other.getJavaClass() == Object.class) {
            return TO_OBJECT.value();
        }
        return null;
    }

    @Override
    public int getReturnInst() {
        return IRETURN;
    }

    @Override
    public Class<?> getBoxedVersion() {
        return Boolean.class;
    }

    @Override
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Boolean.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Boolean.class), Type.BOOLEAN_TYPE),
                false);
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Boolean.class),
                "booleanValue",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE),
                false);
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(METHOD_TO_STRING.value());
    }

    @Override
    public SReference getReferenceType() {
        return SReference.BOOLEAN;
    }

    @Override
    public Class<?> getReferenceClass() {
        return BooleanReference.class;
    }

    @Override
    public String toString() {
        return "boolean";
    }

    private static final Lazy<CastOperation> TO_OBJECT = new Lazy<>(() -> new CastOperation(SType.fromJavaType(Object.class)) {
        @Override
        public void apply(MethodVisitor visitor) {
            SBoolean.instance.compileBoxing(visitor);
        }
    });

    private static final Lazy<BinaryOperation> BITWISE_OR = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_OR, SBoolean.instance, IOR));

    private static final Lazy<BinaryOperation> BITWISE_AND = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_AND, SBoolean.instance, IAND));

    private static final Lazy<UnaryOperation> NOT = new Lazy<>(() -> new UnaryOperation(UnaryOperator.NOT, SBoolean.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            Label elseLabel = new Label();
            Label endLabel = new Label();
            visitor.visitJumpInsn(IFNE, elseLabel);
            visitor.visitInsn(ICONST_1);
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            visitor.visitInsn(ICONST_0);
            visitor.visitLabel(endLabel);
        }
    });

    private static final Lazy<BinaryOperation> BOOLEAN_OR = new Lazy<>(() -> new BinaryOperation(BinaryOperator.BOOLEAN_OR, SBoolean.instance, SBoolean.instance, SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            Label returnTrue = new Label();
            Label end = new Label();
            left.visitJumpInsn(IFNE, returnTrue);
            right.release(left);
            left.visitJumpInsn(GOTO, end);
            left.visitLabel(returnTrue);
            left.visitInsn(ICONST_1);
            left.visitLabel(end);
        }
    });

    private static final Lazy<BinaryOperation> BOOLEAN_AND = new Lazy<>(() -> new BinaryOperation(BinaryOperator.BOOLEAN_AND, SBoolean.instance, SBoolean.instance, SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            Label returnFalse = new Label();
            Label end = new Label();
            left.visitJumpInsn(IFEQ, returnFalse);
            right.release(left);
            left.visitJumpInsn(GOTO, end);
            left.visitLabel(returnFalse);
            left.visitInsn(ICONST_0);
            left.visitLabel(end);
        }
    });

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            """
                    Returns a string representation of a boolean
                    """,
            Boolean.class,
            SBoolean.instance,
            "toString",
            SString.instance));
}