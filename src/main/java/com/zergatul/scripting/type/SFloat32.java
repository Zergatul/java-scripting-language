package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.Float32Reference;
import com.zergatul.scripting.runtime.Float32Utils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import com.zergatul.scripting.type.operation.SingleInstructionBinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SFloat32 extends SPredefinedType {

    public static final SFloat32 instance = new SFloat32();

    private SFloat32() {
        super(float.class);
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public int getLoadInst() {
        return FLOAD;
    }

    @Override
    public int getStoreInst() {
        return FSTORE;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitLdcInsn(0.0f);
    }

    @Override
    public int getArrayTypeInst() {
        return T_FLOAT;
    }

    @Override
    public int getArrayLoadInst() {
        return FALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return FASTORE;
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
    public CastOperation implicitCastTo(SType other) {
        if (other == SFloat.instance) {
            return TO_FLOAT.value();
        }
        if (other instanceof SClassType && other.getJavaClass() == Object.class) {
            return TO_OBJECT.value();
        }
        return null;
    }

    @Override
    public int getReturnInst() {
        return FRETURN;
    }

    @Override
    public Class<?> getBoxedVersion() {
        return Float.class;
    }

    @Override
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Float.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Float.class), Type.FLOAT_TYPE),
                false);
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Float.class),
                "floatValue",
                Type.getMethodDescriptor(Type.FLOAT_TYPE),
                false);
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
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
        return SReference.FLOAT32;
    }

    @Override
    public Class<?> getReferenceClass() {
        return Float32Reference.class;
    }

    @Override
    public String toString() {
        return "float32";
    }

    private static final Lazy<BinaryOperation> ADD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.PLUS, SFloat32.instance, FADD));

    private static final Lazy<BinaryOperation> SUB = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MINUS, SFloat32.instance, FSUB));

    private static final Lazy<BinaryOperation> MUL = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MULTIPLY, SFloat32.instance, FMUL));

    private static final Lazy<BinaryOperation> DIV = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.DIVIDE, SFloat32.instance, FDIV));

    private static final Lazy<BinaryOperation> MOD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MODULO, SFloat32.instance, FREM));

    private static final Lazy<BinaryOperation> LESS_THAN = new Lazy<>(() ->
            new Float32ComparisonOperation(BinaryOperator.LESS, IF_ICMPLT));

    private static final Lazy<BinaryOperation> GREATER_THAN = new Lazy<>(() ->
            new Float32ComparisonOperation(BinaryOperator.GREATER, IF_ICMPGT));

    private static final Lazy<BinaryOperation> LESS_THAN_EQUALS = new Lazy<>(() ->
            new Float32ComparisonOperation(BinaryOperator.LESS_EQUALS, IF_ICMPLE));

    private static final Lazy<BinaryOperation> GREATER_THAN_EQUALS = new Lazy<>(() ->
            new Float32ComparisonOperation(BinaryOperator.GREATER_EQUALS, IF_ICMPGE));

    private static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new Float32ComparisonOperation(BinaryOperator.EQUALS, IF_ICMPEQ));

    private static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new Float32ComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ICMPNE));

    private static final Lazy<UnaryOperation> PLUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.PLUS, SFloat32.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<CastOperation> TO_FLOAT = new Lazy<>(() -> new CastOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(F2D);
        }
    });

    private static final Lazy<CastOperation> TO_OBJECT = new Lazy<>(() -> new CastOperation(SType.fromJavaType(Object.class)) {
        @Override
        public void apply(MethodVisitor visitor) {
            SFloat32.instance.compileBoxing(visitor);
        }
    });

    private static final Lazy<UnaryOperation> MINUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.MINUS, SFloat32.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(FNEG);
        }
    });

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            Float.class,
            SFloat32.instance,
            "toString",
            SString.instance));

    private static final Lazy<MethodReference> METHOD_TO_STANDARD_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            Float32Utils.class,
            SFloat32.instance,
            "toStandardString",
            SString.instance,
            new MethodParameter("digits", SInt.instance)));

    private static final Lazy<MethodReference> METHOD_TRY_PARSE = new Lazy<>(() -> new StaticMethodReference(
            Float32Utils.class,
            SFloat32.instance,
            "tryParse",
            SBoolean.instance,
            new MethodParameter("str", SString.instance),
            new MethodParameter("result", SReference.FLOAT32)));

    private static class Float32ComparisonOperation extends BinaryOperation {

        private final int opcode;

        protected Float32ComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SFloat32.instance, SFloat32.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitInsn(FCMPG);
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