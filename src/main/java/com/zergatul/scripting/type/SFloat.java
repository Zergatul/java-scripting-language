package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.FloatReference;
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
        BinaryOperation operation = super.add(other);
        if (operation != null) {
            return operation;
        }

        return other == this ? ADD.value() : null;
    }

    @Override
    public BinaryOperation subtract(SType other) {
        return other == this ? SUB.value() : null;
    }

    @Override
    public BinaryOperation multiply(SType other) {
        return other == this ? MUL.value() : null;
    }

    @Override
    public BinaryOperation divide(SType other) {
        return other == this ? DIV.value() : null;
    }

    @Override
    public BinaryOperation modulo(SType other) {
        return other == this ? MOD.value() : null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == this ? LESS_THAN.value() : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == this ? GREATER_THAN.value() : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == this ? LESS_THAN_EQUALS.value() : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == this ? GREATER_THAN_EQUALS.value() : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == this ? EQUALS.value() : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == this ? NOT_EQUALS.value() : null;
    }

    @Override
    public UnaryOperation plus() {
        return PLUS.value();
    }

    @Override
    public UnaryOperation minus() {
        return MINUS.value();
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
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Double.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Double.class), Type.DOUBLE_TYPE),
                false);
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
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(METHOD_TO_STRING.value(), METHOD_TO_STANDARD_STRING.value());
    }

    @Override
    public List<MethodReference> getStaticMethods() {
        return List.of(METHOD_TRY_PARSE.value());
    }

    @Override
    public SReference getReferenceType() {
        return SReference.FLOAT;
    }

    @Override
    public Class<?> getReferenceClass() {
        return FloatReference.class;
    }

    @Override
    public String toString() {
        return "float";
    }

    private static final Lazy<BinaryOperation> ADD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.PLUS, SFloat.instance, DADD));

    private static final Lazy<BinaryOperation> SUB = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MINUS, SFloat.instance, DSUB));

    private static final Lazy<BinaryOperation> MUL = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MULTIPLY, SFloat.instance, DMUL));

    private static final Lazy<BinaryOperation> DIV = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.DIVIDE, SFloat.instance, DDIV));

    private static final Lazy<BinaryOperation> MOD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MODULO, SFloat.instance, DREM));

    private static final Lazy<BinaryOperation> LESS_THAN = new Lazy<>(() ->
            new FloatComparisonOperation(BinaryOperator.LESS, IF_ICMPLT));

    private static final Lazy<BinaryOperation> GREATER_THAN = new Lazy<>(() ->
            new FloatComparisonOperation(BinaryOperator.GREATER, IF_ICMPGT));

    private static final Lazy<BinaryOperation> LESS_THAN_EQUALS = new Lazy<>(() ->
            new FloatComparisonOperation(BinaryOperator.LESS_EQUALS, IF_ICMPLE));

    private static final Lazy<BinaryOperation> GREATER_THAN_EQUALS = new Lazy<>(() ->
            new FloatComparisonOperation(BinaryOperator.GREATER_EQUALS, IF_ICMPGE));

    private static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new FloatComparisonOperation(BinaryOperator.EQUALS, IF_ICMPEQ));

    private static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new FloatComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ICMPNE));

    private static final Lazy<UnaryOperation> PLUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.PLUS, SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<UnaryOperation> MINUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.MINUS, SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(DNEG);
        }
    });

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            Double.class,
            SFloat.instance,
            "toString",
            SString.instance));

    private static final Lazy<MethodReference> METHOD_TO_STANDARD_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            FloatUtils.class,
            SFloat.instance,
            "toStandardString",
            SString.instance,
            new MethodParameter("digits", SInt.instance)));

    private static final Lazy<MethodReference> METHOD_TRY_PARSE = new Lazy<>(() -> new StaticMethodReference(
            FloatUtils.class,
            SFloat.instance,
            "tryParse",
            SBoolean.instance,
            new MethodParameter("str", SString.instance),
            new MethodParameter("result", SReference.FLOAT)));

    private static class FloatComparisonOperation extends BinaryOperation {

        private final int opcode;

        protected FloatComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance);
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