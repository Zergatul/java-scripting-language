package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IntOperations;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class SIntType extends SPredefinedType {

    public static final SIntType instance = new SIntType();

    private SIntType() {
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
        return other == this ? IntOperations.ADD : null;
    }

    @Override
    public BinaryOperation subtract(SType other) {
        return other == this ? IntOperations.SUB : null;
    }

    @Override
    public BinaryOperation multiply(SType other) {
        return other == this ? IntOperations.MUL : null;
    }

    @Override
    public BinaryOperation divide(SType other) {
        return other == this ? IntOperations.DIV : null;
    }

    @Override
    public BinaryOperation modulo(SType other) {
        return other == this ? IntOperations.MOD : null;
    }

    /*@Override
    public BinaryOperation floorDiv(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_FLOORDIV_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation floorMod(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_FLOORMOD_INT;
        }
        return null;
    }*/

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == this ? IntOperations.LT : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == this ? IntOperations.GT : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == this ? IntOperations.LTE : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == this ? IntOperations.GTE : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == this ? IntOperations.EQ : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == this ? IntOperations.NEQ : null;
    }

    @Override
    public BinaryOperation bitwiseAnd(SType other) {
        return other == this ? IntOperations.BITWISE_AND : null;
    }

    @Override
    public BinaryOperation bitwiseOr(SType other) {
        return other == this ? IntOperations.BITWISE_OR : null;
    }

    @Override
    public UnaryOperation plus() {
        return IntOperations.PLUS;
    }

    @Override
    public UnaryOperation minus() {
        return IntOperations.MINUS;
    }

    @Override
    public UnaryOperation increment() {
        return IntOperations.INC;
    }

    @Override
    public UnaryOperation decrement() {
        return IntOperations.DEC;
    }

    @Override
    public UnaryOperation implicitCastTo(SType other) {
        return other == SFloatType.instance ? IntOperations.TO_FLOAT : null;
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
    public String toString() {
        return "int";
    }
}