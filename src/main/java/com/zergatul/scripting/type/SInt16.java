package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.Int16Reference;
import com.zergatul.scripting.runtime.Int16Utils;
import com.zergatul.scripting.type.operation.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SInt16 extends SPredefinedType {

    public static final SInt16 instance = new SInt16();

    private SInt16() {
        super(short.class);
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
    public boolean hasDefaultValue() {
        return true;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitInsn(ICONST_0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_SHORT;
    }

    @Override
    public int getArrayLoadInst() {
        return SALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return SASTORE;
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
    public CastOperation implicitCastTo(SType other) {
        if (other == SInt64.instance) {
            return TO_INT64.value();
        }
        if (other == SInt.instance) {
            return TO_INT32.value();
        }
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
        return IRETURN;
    }

    @Override
    public Class<?> getBoxedVersion() {
        return Short.class;
    }

    @Override
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Short.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Short.class), Type.SHORT_TYPE),
                false);
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Short.class),
                "shortValue",
                Type.getMethodDescriptor(Type.SHORT_TYPE),
                false);
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
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
        return SByReference.INT16;
    }

    @Override
    public Class<?> getReferenceClass() {
        return Int16Reference.class;
    }

    @Override
    public String toString() {
        return "int16";
    }

    private static final Lazy<BinaryOperation> ADD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.PLUS, SInt16.instance, SInt.instance, IADD));

    private static final Lazy<BinaryOperation> SUB = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MINUS, SInt16.instance, SInt.instance, ISUB));

    private static final Lazy<BinaryOperation> MUL = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MULTIPLY, SInt16.instance, SInt.instance, IMUL));

    private static final Lazy<BinaryOperation> DIV = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.DIVIDE, SInt16.instance, SInt.instance, IDIV));

    private static final Lazy<BinaryOperation> MOD = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.MODULO, SInt16.instance, SInt.instance, IREM));

    private static final Lazy<BinaryOperation> BITWISE_AND = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_AND, SInt16.instance, SInt.instance, IAND));

    private static final Lazy<BinaryOperation> BITWISE_OR = new Lazy<>(() ->
            new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_OR, SInt16.instance, SInt.instance, IOR));

    private static final Lazy<BinaryOperation> LESS_THAN = new Lazy<>(() ->
            new Int16ComparisonOperation(BinaryOperator.LESS, IF_ICMPLT));

    private static final Lazy<BinaryOperation> GREATER_THAN = new Lazy<>(() ->
            new Int16ComparisonOperation(BinaryOperator.GREATER, IF_ICMPGT));

    private static final Lazy<BinaryOperation> LESS_THAN_EQUALS = new Lazy<>(() ->
            new Int16ComparisonOperation(BinaryOperator.LESS_EQUALS, IF_ICMPLE));

    private static final Lazy<BinaryOperation> GREATER_THAN_EQUALS = new Lazy<>(() ->
            new Int16ComparisonOperation(BinaryOperator.GREATER_EQUALS, IF_ICMPGE));

    private static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new Int16ComparisonOperation(BinaryOperator.EQUALS, IF_ICMPEQ));

    private static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new Int16ComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ICMPNE));

    private static final Lazy<UnaryOperation> PLUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.PLUS, SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<UnaryOperation> MINUS = new Lazy<>(() -> new UnaryOperation(UnaryOperator.MINUS, SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(INEG);
        }
    });

    private static final Lazy<CastOperation> TO_FLOAT32 = new Lazy<>(() -> new CastOperation(SFloat32.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(I2F);
        }
    });

    private static final Lazy<CastOperation> TO_FLOAT = new Lazy<>(() -> new CastOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(I2D);
        }
    });

    private static final Lazy<CastOperation> TO_INT64 = new Lazy<>(() -> new CastOperation(SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(I2L);
        }
    });

    private static final Lazy<CastOperation> TO_INT32 = new Lazy<>(() -> new CastOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<CastOperation> TO_OBJECT = new Lazy<>(() -> new CastOperation(SJavaObject.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            SInt16.instance.compileBoxing(visitor);
        }
    });

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            """
                    Returns a string representation of an integer
                    """,
            Short.class,
            SInt16.instance,
            "toString",
            SString.instance));

    private static final Lazy<MethodReference> METHOD_TO_STANDARD_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            Int16Utils.class,
            SInt16.instance,
            "toStandardString",
            SString.instance));

    private static final Lazy<MethodReference> METHOD_TRY_PARSE = new Lazy<>(() -> new StaticMethodReference(
            Int16Utils.class,
            SInt16.instance,
            "tryParse",
            SBoolean.instance,
            new MethodParameter("str", SString.instance),
            new MethodParameter("result", SByReference.INT16)));

    private static final Lazy<PropertyReference> PROPERTY_MIN_VALUE = new Lazy<>(() -> new GetterPropertyReference(
            SInt16.instance,
            "MIN_VALUE",
            visitor -> visitor.visitLdcInsn(Short.MIN_VALUE)));

    private static final Lazy<PropertyReference> PROPERTY_MAX_VALUE = new Lazy<>(() -> new GetterPropertyReference(
            SInt16.instance,
            "MAX_VALUE",
            visitor -> visitor.visitLdcInsn(Short.MAX_VALUE)));

    private static class Int16ComparisonOperation extends BinaryOperation {

        private final int opcode;

        public Int16ComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SInt16.instance, SInt16.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
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