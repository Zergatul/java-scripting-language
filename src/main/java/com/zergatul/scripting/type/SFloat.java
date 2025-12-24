package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
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

public class SFloat extends SValueType {

    public static final SFloat instance = new SFloat();

    private final SBoxedType boxed = new SBoxedType(this, Double.class);
    private final Lazy<List<BinaryOperation>> binaryOperations = new Lazy<>(this::getBinaryOperationsInternal);
    private final Lazy<List<CastOperation>> implicitCasts = new Lazy<>(this::getImplicitCastsInternal);

    private SFloat() {
        super(double.class);
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
    public boolean hasDefaultValue() {
        return true;
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
    public List<UnaryOperation> getUnaryOperations() {
        return List.of(PLUS.value(), MINUS.value());
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return binaryOperations.value();
    }

    private List<BinaryOperation> getBinaryOperationsInternal() {
        return List.of(
                ADD.value(),
                SUB.value(),
                MUL.value(),
                DIV.value(),
                MOD.value(),
                LESS_THAN.value(),
                GREATER_THAN.value(),
                LESS_THAN_EQUALS.value(),
                GREATER_THAN_EQUALS.value(),
                EQUALS.value(),
                NOT_EQUALS.value());
    }

    @Override
    public List<CastOperation> getImplicitCasts() {
        return implicitCasts.value();
    }

    private List<CastOperation> getImplicitCastsInternal() {
        return extendWithBoxing(
                FLOAT64_TO_BOXED.value());
    }

    @Override
    public int getReturnInst() {
        return DRETURN;
    }

    @Override
    public SBoxedType getBoxed() {
        return boxed;
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
    public SByReference getReferenceType() {
        return SByReference.FLOAT;
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

    private static final Lazy<CastOperation> FLOAT64_TO_BOXED = new Lazy<>(() -> new CastOperation(instance, instance.boxed) {
        @Override
        public void apply(MethodVisitor visitor) {
            SFloat.instance.compileBoxing(visitor);
        }
    });

    private static final Lazy<UnaryOperation> PLUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.PLUS, SFloat.instance, instance) {
        @Override
        public void apply(MethodVisitor visitor, CompilerContext context) {}
    });

    private static final Lazy<UnaryOperation> MINUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.MINUS, SFloat.instance, instance) {
        @Override
        public void apply(MethodVisitor visitor, CompilerContext context) {
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
            new MethodParameter("result", SByReference.FLOAT)));

    private static class FloatComparisonOperation extends BinaryOperation {

        private final int opcode;

        protected FloatComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SFloat.instance, SFloat.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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