package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SChar extends SPredefinedType {

    public static final SChar instance = new SChar();

    private SChar() {
        super(char.class);
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
        visitor.visitLdcInsn((char) 0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_CHAR;
    }

    @Override
    public int getArrayLoadInst() {
        return CALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return CASTORE;
    }

    @Override
    public int getReturnInst() {
        return IRETURN;
    }

    @Override
    public UnaryOperation implicitCastTo(SType other) {
        return other == SIntType.instance ? CHAR_TO_INT : null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == SChar.instance ? SIntType.instance.lessThan(SIntType.instance) : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == SChar.instance ? SIntType.instance.greaterThan(SIntType.instance) : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == SChar.instance ? SIntType.instance.lessEquals(SIntType.instance) : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == SChar.instance ? SIntType.instance.greaterEquals(SIntType.instance) : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == SChar.instance ? SIntType.instance.equalsOp(SIntType.instance) : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == SChar.instance ? SIntType.instance.notEqualsOp(SIntType.instance) : null;
    }

    @Override
    public String toString() {
        return "char";
    }

    private static final UnaryOperation CHAR_TO_INT = new UnaryOperation(SIntType.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };
}