package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.BooleanOperations;
import com.zergatul.scripting.type.operation.IntOperations;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

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
    public BinaryOperation booleanAnd(SType other) {
        return other == this ? BooleanOperations.BOOLEAN_AND : null;
    }

    @Override
    public BinaryOperation booleanOr(SType other) {
        return other == this ? BooleanOperations.BOOLEAN_OR : null;
    }

    @Override
    public BinaryOperation bitwiseAnd(SType other) {
        return other == this ? BooleanOperations.BITWISE_AND : null;
    }

    @Override
    public BinaryOperation bitwiseOr(SType other) {
        return other == this ? BooleanOperations.BITWISE_OR : null;
    }

    @Override
    public UnaryOperation not() {
        return BooleanOperations.NOT;
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
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Boolean.class),
                "booleanValue",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE),
                false);
    }

    @Override
    public String toString() {
        return "boolean";
    }
}