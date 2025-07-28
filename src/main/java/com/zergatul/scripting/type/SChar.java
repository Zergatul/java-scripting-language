package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

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
    public Class<?> getBoxedVersion() {
        return Character.class;
    }

    @Override
    public void compileBoxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Character.class),
                "valueOf",
                Type.getMethodDescriptor(Type.getType(Character.class), Type.CHAR_TYPE),
                false);
    }

    @Override
    public void compileUnboxing(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(Character.class),
                "charValue",
                Type.getMethodDescriptor(Type.CHAR_TYPE),
                false);
    }

    @Override
    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(METHOD_TO_STRING.value());
    }

    @Override
    public CastOperation implicitCastTo(SType other) {
        return other == SInt.instance ? CHAR_TO_INT.value() : null;
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

    private static final Lazy<CastOperation> CHAR_TO_INT = new Lazy<>(() -> new CastOperation(SInt.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    });

    private static final Lazy<MethodReference> METHOD_TO_STRING = new Lazy<>(() -> new StaticAsInstanceMethodReference(
            """
                    Returns a string containing single character
                    """,
            String.class,
            SChar.instance,
            "valueOf",
            "toString",
            SString.instance));
}