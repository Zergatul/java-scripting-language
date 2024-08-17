package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SChar extends SPredefinedType {

    public static final SChar instance = new SChar();
    public static final SStaticTypeReference staticRef = new SStaticTypeReference(instance);

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
    public List<MethodReference> getInstanceMethods() {
        return List.of(METHOD_TO_STRING);
    }

    @Override
    public CastOperation implicitCastTo(SType other) {
        return other == SInt.instance ? CHAR_TO_INT : null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        return other == SChar.instance ? SInt.instance.lessThan(SInt.instance) : null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        return other == SChar.instance ? SInt.instance.greaterThan(SInt.instance) : null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        return other == SChar.instance ? SInt.instance.lessEquals(SInt.instance) : null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        return other == SChar.instance ? SInt.instance.greaterEquals(SInt.instance) : null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        String.valueOf('c');
        return other == SChar.instance ? SInt.instance.equalsOp(SInt.instance) : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == SChar.instance ? SInt.instance.notEqualsOp(SInt.instance) : null;
    }

    @Override
    public String toString() {
        return "char";
    }

    private static final CastOperation CHAR_TO_INT = new CastOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };

    private static final MethodReference METHOD_TO_STRING = new StaticAsInstanceMethodReference(
            """
                    Returns a string containing single character
                    """,
            String.class,
            SChar.instance,
            "valueOf",
            "toString",
            SString.instance);
}