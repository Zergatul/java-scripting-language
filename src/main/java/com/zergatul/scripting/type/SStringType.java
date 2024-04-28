package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class SStringType extends SPredefinedType {

    public static final SStringType instance = new SStringType();

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
            return ADD_STRING;
        }
        if (other == SChar.instance) {
            return ADD_CHAR;
        }
        return null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        return other == SStringType.instance ? EQUALS_STRING : null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        return other == SStringType.instance ? NOT_EQUALS_STRING : null;
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        return switch (name) {
            case "length" -> PROP_LENGTH;
            default -> null;
        };
    }

    @Override
    public List<SType> supportedIndexers() {
        return List.of(SIntType.instance);
    }

    @Override
    public IndexOperation index(SType type) {
        if (type == SIntType.instance) {
            return INDEX_INT;
        } else {
            return null;
        }
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public String toString() {
        return "string";
    }

    private static final PropertyReference PROP_LENGTH = new MethodBasedPropertyReference(String.class, "length");

    private static final BinaryOperation ADD_STRING = new BinaryOperation(SStringType.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            // stack = ..., left
            left.visitTypeInsn(NEW, Type.getInternalName(StringBuilder.class));
            // stack = ..., left, builder
            left.visitInsn(DUP);
            // stack = ..., left, builder, builder
            left.visitMethodInsn(
                    INVOKESPECIAL,
                    Type.getInternalName(StringBuilder.class),
                    "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE),
                    false);
            // stack = ..., left, builder
            left.visitInsn(SWAP);
            // stack = ..., builder, left
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    "append",
                    Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(String.class)),
                    false);
            // stack = ..., builder
            right.release(left);
            // stack = ..., builder, left
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    "append",
                    Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(String.class)),
                    false);
            // stack = ..., builder
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    "toString",
                    Type.getMethodDescriptor(Type.getType(String.class)),
                    false);
        }
    };

    private static final BinaryOperation ADD_CHAR = new BinaryOperation(SStringType.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            // stack = ..., left
            left.visitTypeInsn(NEW, Type.getInternalName(StringBuilder.class));
            // stack = ..., left, builder
            left.visitInsn(DUP);
            // stack = ..., left, builder, builder
            left.visitMethodInsn(
                    INVOKESPECIAL,
                    Type.getInternalName(StringBuilder.class),
                    "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE),
                    false);
            // stack = ..., left, builder
            left.visitInsn(SWAP);
            // stack = ..., builder, left
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    "append",
                    Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(String.class)),
                    false);
            // stack = ..., builder
            right.release(left);
            // stack = ..., builder, left
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    "append",
                    Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.CHAR_TYPE),
                    false);
            // stack = ..., builder
            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    "toString",
                    Type.getMethodDescriptor(Type.getType(String.class)),
                    false);
        }
    };

    private static final BinaryOperation EQUALS_STRING = new BinaryOperation(SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Objects.class),
                    "equals",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class), Type.getType(Object.class)),
                    false);
        }
    };

    private static final BinaryOperation NOT_EQUALS_STRING = new BinaryOperation(SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            SStringType.EQUALS_STRING.apply(left, right);
            SBoolean.instance.not().apply(left);
        }
    };

    private static final IndexOperation INDEX_INT = new IndexOperation(SChar.instance) {
        @Override
        public boolean canGet() {
            return true;
        }

        @Override
        public void compileGet(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "charAt",
                    Type.getMethodDescriptor(Type.CHAR_TYPE, Type.INT_TYPE),
                    false);
        }

        @Override
        public void compileSet(MethodVisitor visitor) {
            throw new InternalException();
        }
    };
}