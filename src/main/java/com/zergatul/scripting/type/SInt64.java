package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
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
    public static final SStaticTypeReference staticRef = new SStaticTypeReference(instance);

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

    @Override
    public BinaryOperation add(SType other) {
        BinaryOperation operation = super.add(other);
        if (operation != null) {
            return operation;
        }

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
    public PostfixOperation increment() {
        return INC;
    }

    @Override
    public PostfixOperation decrement() {
        return DEC;
    }

    @Override
    public CastOperation implicitCastTo(SType other) {
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
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Integer.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Integer.class), Type.INT_TYPE),
                false);
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
    public List<PropertyReference> getStaticProperties() {
        return List.of(PROPERTY_MIN_VALUE, PROPERTY_MAX_VALUE);
    }

    @Override
    public SReference getReferenceType() {
        return SReference.INT64;
    }

    @Override
    public Class<?> getReferenceClass() {
        return Int64Reference.class;
    }

    @Override
    public String toString() {
        return "int64";
    }

    private static final BinaryOperation ADD = new SingleInstructionBinaryOperation(BinaryOperator.PLUS, SInt64.instance, LADD);
    private static final BinaryOperation SUB = new SingleInstructionBinaryOperation(BinaryOperator.MINUS, SInt64.instance, LSUB);
    private static final BinaryOperation MUL = new SingleInstructionBinaryOperation(BinaryOperator.MULTIPLY, SInt64.instance, LMUL);
    private static final BinaryOperation DIV = new SingleInstructionBinaryOperation(BinaryOperator.DIVIDE, SInt64.instance, LDIV);
    private static final BinaryOperation MOD = new SingleInstructionBinaryOperation(BinaryOperator.MODULO, SInt64.instance, LREM);
    private static final BinaryOperation BITWISE_AND = new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_AND, SInt64.instance, LAND);
    private static final BinaryOperation BITWISE_OR = new SingleInstructionBinaryOperation(BinaryOperator.BITWISE_OR, SInt64.instance, LOR);
    private static final BinaryOperation LESS_THAN = new Int64ComparisonOperation(BinaryOperator.LESS, IF_ICMPLT);
    private static final BinaryOperation GREATER_THAN = new Int64ComparisonOperation(BinaryOperator.GREATER, IF_ICMPGT);
    private static final BinaryOperation LESS_THAN_EQUALS = new Int64ComparisonOperation(BinaryOperator.LESS_EQUALS, IF_ICMPLE);
    private static final BinaryOperation GREATER_THAN_EQUALS = new Int64ComparisonOperation(BinaryOperator.GREATER_EQUALS, IF_ICMPGE);
    private static final BinaryOperation EQUALS = new Int64ComparisonOperation(BinaryOperator.EQUALS, IF_ICMPEQ);
    private static final BinaryOperation NOT_EQUALS = new Int64ComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ICMPNE);
    private static final UnaryOperation PLUS = new UnaryOperation(UnaryOperator.PLUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };
    private static final UnaryOperation MINUS = new UnaryOperation(UnaryOperator.MINUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(LNEG);
        }
    };
    private static final PostfixOperation INC = new PostfixOperation(PostfixOperator.PLUS_PLUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(LCONST_1);
            visitor.visitInsn(LADD);
        }
    };
    private static final PostfixOperation DEC = new PostfixOperation(PostfixOperator.MINUS_MINUS, SInt64.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(LCONST_1);
            visitor.visitInsn(LSUB);
        }
    };
    private static final CastOperation TO_FLOAT = new CastOperation(SFloat.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(L2D);
        }
    };

    private static final MethodReference METHOD_TO_STRING = new StaticAsInstanceMethodReference(
            """
                    Returns a string representation of an 64bit integer
                    """,
            Long.class,
            SInt64.instance,
            "toString",
            SString.instance);

    private static final MethodReference METHOD_TO_STANDARD_STRING = new StaticAsInstanceMethodReference(
            Int64Utils.class,
            SInt64.instance,
            "toStandardString",
            SString.instance);

    private static final MethodReference METHOD_TRY_PARSE = new StaticMethodReference(
            Int64Utils.class,
            SInt64.instance,
            "tryParse",
            SBoolean.instance,
            new MethodParameter("str", SString.instance),
            new MethodParameter("result", SReference.INT64));

    private static final PropertyReference PROPERTY_MIN_VALUE = new GetterPropertyReference(
            SInt64.instance,
            "MIN_VALUE",
            visitor -> visitor.visitLdcInsn(Long.MIN_VALUE));

    private static final PropertyReference PROPERTY_MAX_VALUE = new GetterPropertyReference(
            SInt64.instance,
            "MAX_VALUE",
            visitor -> visitor.visitLdcInsn(Long.MAX_VALUE));

    private static class Int64ComparisonOperation extends BinaryOperation {

        private final int opcode;

        public Int64ComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SInt64.instance);
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
