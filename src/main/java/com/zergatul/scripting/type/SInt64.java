package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.PostfixOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.Int64Reference;
import com.zergatul.scripting.runtime.Int64Utils;
import com.zergatul.scripting.type.operation.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SInt64 extends SPredefinedType {

    public static final SInt64 instance = new SInt64();

    private SInt64() {
        super(long.class);
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public int getLoadInst() {
        return LLOAD;
    }

    @Override
    public int getStoreInst() {
        return LSTORE;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitInsn(LCONST_0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_LONG;
    }

    @Override
    public int getArrayLoadInst() {
        return LALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return LASTORE;
    }

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
    public BinaryOperation bitwiseAnd(SType other) {
        return other == this ? BITWISE_AND.value() : null;
    }

    @Override
    public BinaryOperation bitwiseOr(SType other) {
        return other == this ? BITWISE_OR.value() : null;
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
    public PostfixOperation increment() {
        return INC.value();
    }

    @Override
    public PostfixOperation decrement() {
        return DEC.value();
    }

    @Override
    public CastOperation implicitCastTo(SType other) {
        if (other == SFloat32.instance) {
            return TO_FLOAT32.value();
        }
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
        return LRETURN;
    }

    @Override
    public Class<?> getBoxedVersion() {
        return Long.class;
    }

    @Override
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Long.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Long.class), Type.LONG_TYPE),
                false);
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Long.class),
                "longValue",
                Type.getMethodDescriptor(Type.LONG_TYPE),
                false);
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
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
    public List<PropertyReference> getStaticProperties() {
        return List.of(PROPERTY_MIN_VALUE.value(), PROPERTY_MAX_VALUE.value());
    }

    @Override
    public SByReference getReferenceType() {
        return SByReference.INT64;
    }

    @Override
    public Class<?> getReferenceClass() {
        return Int64Reference.class;
    }

    @Override
    public String toString() {
        return "int64";
    }

    private static final Lazy<BinaryOperation> ADD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.PLUS, SInt64.instance, LADD));

    private static final Lazy<BinaryOperation> SUB = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MINUS, SInt64.instance, LSUB));

    private static final Lazy<BinaryOperation> MUL = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MULTIPLY, SInt64.instance, LMUL));

    private static final Lazy<BinaryOperation> DIV = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.DIVIDE, SInt64.instance, LDIV));

    private static final Lazy<BinaryOperation> MOD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MODULO, SInt64.instance, LREM));

    private static final Lazy<BinaryOperation> BITWISE_AND = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_AND, SInt64.instance, LAND));

    private static final Lazy<BinaryOperation> BITWISE_OR = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_OR, SInt64.instance, LOR));

    private static final Lazy<BinaryOperation> LESS_THAN = new Lazy<>(() ->
            new Int64ComparisonOperation(BinaryOperator.LESS, IFLT));

    private static final Lazy<BinaryOperation> GREATER_THAN = new Lazy<>(() ->
            new Int64ComparisonOperation(BinaryOperator.GREATER, IFGT));

    private static final Lazy<BinaryOperation> LESS_THAN_EQUALS = new Lazy<>(() ->
            new Int64ComparisonOperation(BinaryOperator.LESS_EQUALS, IFLE));

    private static final Lazy<BinaryOperation> GREATER_THAN_EQUALS = new Lazy<>(() ->
            new Int64ComparisonOperation(BinaryOperator.GREATER_EQUALS, IFGE));

    private static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new Int64ComparisonOperation(BinaryOperator.EQUALS, IFEQ));

    private static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new Int64ComparisonOperation(BinaryOperator.NOT_EQUALS, IFNE));

    private static final Lazy<UnaryOperation> PLUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.PLUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<UnaryOperation> MINUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.MINUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(LNEG);
        }
    });

    private static final Lazy<PostfixOperation> INC = new Lazy<>(() -> new PostfixOperation(PostfixOperator.PLUS_PLUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(LCONST_1);
            visitor.visitInsn(LADD);
        }
    });

    private static final Lazy<PostfixOperation> DEC = new Lazy<>(() -> new PostfixOperation(PostfixOperator.MINUS_MINUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(LCONST_1);
            visitor.visitInsn(LSUB);
        }
    });

    private static final Lazy<CastOperation> TO_FLOAT32 = new Lazy<>(() -> new CastOperation(SFloat32.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(L2F);
        }
    });

    private static final Lazy<CastOperation> TO_FLOAT = new Lazy<>(() -> new CastOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(L2D);
        }
    });

    private static final Lazy<CastOperation> TO_OBJECT = new Lazy<>(() -> new CastOperation(SType.fromJavaType(Object.class)) {
        @Override
        public void apply(MethodVisitor visitor) {
            SInt64.instance.compileBoxing(visitor);
        }
    });

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            """
                    Returns a string representation of an 64bit integer
                    """,
            Long.class,
            SInt64.instance,
            "toString",
            SString.instance));

    private static final Lazy<MethodReference> METHOD_TO_STANDARD_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            Int64Utils.class,
            SInt64.instance,
            "toStandardString",
            SString.instance));

    private static final Lazy<MethodReference> METHOD_TRY_PARSE = new Lazy<>(() -> new StaticMethodReference(
            Int64Utils.class,
            SInt64.instance,
            "tryParse",
            SBoolean.instance,
            new MethodParameter("str", SString.instance),
            new MethodParameter("result", SByReference.INT64)));

    private static final Lazy<PropertyReference> PROPERTY_MIN_VALUE = new Lazy<>(() -> new GetterPropertyReference(
            SInt64.instance,
            "MIN_VALUE",
            visitor -> visitor.visitLdcInsn(Long.MIN_VALUE)));

    private final Lazy<PropertyReference> PROPERTY_MAX_VALUE = new Lazy<>(() -> new GetterPropertyReference(
            SInt64.instance,
            "MAX_VALUE",
            visitor -> visitor.visitLdcInsn(Long.MAX_VALUE)));

    private static class Int64ComparisonOperation extends BinaryOperation {

        private final int opcode;

        public Int64ComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SInt64.instance, SInt64.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
            right.release(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitInsn(LCMP);
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }
}