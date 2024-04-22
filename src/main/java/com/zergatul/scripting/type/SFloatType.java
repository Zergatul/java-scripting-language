package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.FloatOperations;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class SFloatType extends SPredefinedType {

    public static final SFloatType instance = new SFloatType();

    private SFloatType() {
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
    public BinaryOperation add(SType other) {
        return other == this ? FloatOperations.ADD : null;
    }

    @Override
    public BinaryOperation subtract(SType other) {
        return other == this ? FloatOperations.SUB : null;
    }

    @Override
    public BinaryOperation multiply(SType other) {
        return other == this ? FloatOperations.MUL : null;
    }

    @Override
    public BinaryOperation divide(SType other) {
        return other == this ? FloatOperations.DIV : null;
    }

    @Override
    public BinaryOperation modulo(SType other) {
        return other == this ? FloatOperations.MOD : null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == this ? FloatOperations.LT : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == this ? FloatOperations.GT : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == this ? FloatOperations.LTE : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == this ? FloatOperations.GTE : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == this ? FloatOperations.EQ : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == this ? FloatOperations.NEQ : null;
    }

    @Override
    public UnaryOperation plus() {
        return FloatOperations.PLUS;
    }

    @Override
    public UnaryOperation minus() {
        return FloatOperations.MINUS;
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
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Double.class),
                "doubleValue",
                Type.getMethodDescriptor(Type.DOUBLE_TYPE),
                false);
    }

    @Override
    public String toString() {
        return "float";
    }
}