package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.StringOperations;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SStringType extends SPredefinedType {

    public static final SStringType instance = new SStringType();

    private static final PropertyReference length = new MethodBasedPropertyReference(String.class, "length");

    private SStringType() {
        super(String.class);
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitLdcInsn("");
    }

    @Override
    public int getArrayTypeInst() {
        throw new InternalException();
    }

    @Override
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public BinaryOperation add(SType other) {
        if (other == SStringType.instance) {
            return StringOperations.CONCAT;
        }
        return null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == SStringType.instance ? StringOperations.EQ : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == SStringType.instance ? StringOperations.NEQ : null;
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        return switch (name) {
            case "length" -> length;
            default -> null;
        };
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public String toString() {
        return "string";
    }
}