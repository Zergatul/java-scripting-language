package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
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

public class SBoolean extends SValueType {

    public static final SBoolean instance = new SBoolean();

    private final SBoxedType boxed = new SBoxedType(this, Boolean.class);
    private final Lazy<List<BinaryOperation>> binaryOperations = new Lazy<>(this::getBinaryOperationsInternal);
    private final Lazy<List<CastOperation>> implicitCasts = new Lazy<>(this::getImplicitCastsInternal);

    private SBoolean() {
        super(boolean.class);
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
    public boolean hasDefaultValue() {
        return true;
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
    public List<UnaryOperation> getUnaryOperations() {
        return List.of(NOT.value());
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return binaryOperations.value();
    }

    private List<BinaryOperation> getBinaryOperationsInternal() {
        return List.of(
                LESS_THAN.value(),
                GREATER_THAN.value(),
                LESS_THAN_EQUALS.value(),
                GREATER_THAN_EQUALS.value(),
                EQUALS.value(),
                NOT_EQUALS.value(),
                BOOLEAN_AND.value(),
                BOOLEAN_OR.value(),
                BITWISE_AND.value(),
                BITWISE_OR.value());
    }

    @Override
    public List<CastOperation> getImplicitCasts() {
        return implicitCasts.value();
    }

    private List<CastOperation> getImplicitCastsInternal() {
        return extendWithBoxing(
                BOOLEAN_TO_BOXED.value());
    }

    @Override
    public int getReturnInst() {
        return IRETURN;
    }

    @Override
    public SBoxedType getBoxed() {
        return boxed;
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
    public SByReference getReferenceType() {
        return SByReference.BOOLEAN;
    }

    @Override
    public Class<?> getReferenceClass() {
        return BooleanReference.class;
    }

    @Override
    public String toString() {
        return "boolean";
    }

    private static final Lazy<CastOperation> BOOLEAN_TO_BOXED = new Lazy<>(() -> new CastOperation(instance, instance.boxed) {
        @Override
        public void apply(MethodVisitor visitor) {
            SBoolean.instance.compileBoxing(visitor);
        }
    });

    protected static final Lazy<BinaryOperation> LESS_THAN = new Lazy<>(() ->
            new BooleanComparisonOperation(BinaryOperator.LESS, IF_ICMPLT));

    protected static final Lazy<BinaryOperation> GREATER_THAN = new Lazy<>(() ->
            new BooleanComparisonOperation(BinaryOperator.GREATER, IF_ICMPGT));

    protected static final Lazy<BinaryOperation> LESS_THAN_EQUALS = new Lazy<>(() ->
            new BooleanComparisonOperation(BinaryOperator.LESS_EQUALS, IF_ICMPLE));

    public static final Lazy<BinaryOperation> GREATER_THAN_EQUALS = new Lazy<>(() ->
            new BooleanComparisonOperation(BinaryOperator.GREATER_EQUALS, IF_ICMPGE));

    protected static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new BooleanComparisonOperation(BinaryOperator.EQUALS, IF_ICMPEQ));

    protected static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new BooleanComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ICMPNE));

    private static final Lazy<BinaryOperation> BITWISE_OR = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_OR, SBoolean.instance, IOR));

    private static final Lazy<BinaryOperation> BITWISE_AND = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_AND, SBoolean.instance, IAND));

    public static final Lazy<UnaryOperation> NOT = new Lazy<>(() -> new UnaryOperation(UnaryOperator.NOT, SBoolean.instance, SBoolean.instance) {
        @Override
        public void apply(MethodVisitor visitor, CompilerContext context) {
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

    public static final Lazy<BinaryOperation> BOOLEAN_OR = new Lazy<>(() -> new BinaryOperation(BinaryOperator.BOOLEAN_OR, SBoolean.instance, SBoolean.instance, SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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

    public static final Lazy<BinaryOperation> BOOLEAN_AND = new Lazy<>(() -> new BinaryOperation(BinaryOperator.BOOLEAN_AND, SBoolean.instance, SBoolean.instance, SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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

    private static class BooleanComparisonOperation extends BinaryOperation {

        private final int opcode;

        public BooleanComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SBoolean.instance, SBoolean.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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