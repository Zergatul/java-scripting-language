package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.runtime.IntUtils;
import com.zergatul.scripting.runtime.StringUtils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Locale;
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
    public List<MethodReference> getInstanceMethods(String name) {
        return switch (name) {
            case "contains" -> List.of(METHOD_CONTAINS);
            case "indexOf" -> List.of(METHOD_INDEX_OF);
            case "substring" -> List.of(METHOD_SUBSTRING_INT, METHOD_SUBSTRING_INT_INT);
            case "startsWith" -> List.of(METHOD_STARTS_WITH);
            case "endsWith" -> List.of(METHOD_ENDS_WITH);
            case "toLower" -> List.of(METHOD_TO_LOWER);
            case "toUpper" -> List.of(METHOD_TO_UPPER);
            case "matches" -> List.of(METHOD_MATCHES);
            case "getMatches" -> List.of(METHOD_GET_MATCHES);
            default -> List.of();
        };
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

    private static final MethodReference METHOD_SUBSTRING_INT = new MethodReference() {
        @Override
        public SType getReturn() {
            return SStringType.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SIntType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "substring",
                    Type.getMethodDescriptor(Type.getType(String.class), Type.INT_TYPE),
                    false);
        }
    };

    private static final MethodReference METHOD_SUBSTRING_INT_INT = new MethodReference() {
        @Override
        public SType getReturn() {
            return SStringType.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SIntType.instance, SIntType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "substring",
                    Type.getMethodDescriptor(Type.getType(String.class), Type.INT_TYPE, Type.INT_TYPE),
                    false);
        }
    };

    private static final MethodReference METHOD_CONTAINS = new MethodReference() {
        @Override
        public SType getReturn() {
            return SBoolean.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SStringType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "contains",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(CharSequence.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_INDEX_OF = new MethodReference() {
        @Override
        public SType getReturn() {
            return SIntType.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SStringType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "indexOf",
                    Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(String.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_STARTS_WITH = new MethodReference() {
        @Override
        public SType getReturn() {
            return SBoolean.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SStringType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "startsWith",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(String.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_ENDS_WITH = new MethodReference() {
        @Override
        public SType getReturn() {
            return SBoolean.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SStringType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "endsWith",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(String.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_TO_LOWER = new MethodReference() {
        @Override
        public SType getReturn() {
            return SStringType.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of();
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitFieldInsn(
                    GETSTATIC,
                    Type.getInternalName(Locale.class),
                    "ROOT",
                    Type.getDescriptor(Locale.class));
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "toLowerCase",
                    Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Locale.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_TO_UPPER = new MethodReference() {
        @Override
        public SType getReturn() {
            return SStringType.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of();
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitFieldInsn(
                    GETSTATIC,
                    Type.getInternalName(Locale.class),
                    "ROOT",
                    Type.getDescriptor(Locale.class));
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "toUpperCase",
                    Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Locale.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_MATCHES = new MethodReference() {
        @Override
        public SType getReturn() {
            return SBoolean.instance;
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SStringType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(StringUtils.class),
                    "matches",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(String.class), Type.getType(String.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_GET_MATCHES = new MethodReference() {
        @Override
        public SType getReturn() {
            return new SArrayType(SStringType.instance);
        }

        @Override
        public List<SType> getParameters() {
            return List.of(SStringType.instance);
        }

        @Override
        public void compileInvoke(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(StringUtils.class),
                    "getMatches",
                    Type.getMethodDescriptor(Type.getType(String[].class), Type.getType(String.class), Type.getType(String.class)),
                    false);
        }
    };
}