package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.runtime.StringUtils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class SString extends SReferenceType {

    public static final SString instance = new SString();

    private SString() {}

    @Override
    public boolean isPredefined() {
        return true;
    }

    @Override
    public Class<?> getJavaClass() {
        return String.class;
    }

    @Override
    public boolean hasDefaultValue() {
        return true;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitLdcInsn("");
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return List.of(
                STRING_ADD_CHAR,
                STRING_ADD_STRING,
                STRING_EQUALS_STRING,
                STRING_NOT_EQUALS_STRING,
                STRING_ADD_STRING_CONVERTIBLE,
                STRING_CONVERTIBLE_ADD_STRING);
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return List.of(PROP_LENGTH);
    }

    @Override
    public @Nullable PropertyReference getInstanceProperty(String name) {
        return switch (name) {
            case "length" -> PROP_LENGTH;
            default -> null;
        };
    }

    @Override
    public List<SType> supportedIndexers() {
        return List.of(SInt.instance);
    }

    @Override
    public @Nullable IndexOperation index(SType type) {
        if (type == SInt.instance) {
            return INDEX_INT;
        } else {
            return null;
        }
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return List.of(
                METHOD_CONTAINS,
                METHOD_INDEX_OF,
                METHOD_SUBSTRING_INT,
                METHOD_SUBSTRING_INT_INT,
                METHOD_STARTS_WITH,
                METHOD_ENDS_WITH,
                METHOD_TO_LOWER,
                METHOD_TO_UPPER,
                METHOD_MATCHES,
                METHOD_MATCHES_FLAGS,
                METHOD_GET_MATCHES,
                METHOD_GET_MATCHES_FLAGS,
                METHOD_REPLACE,
                METHOD_SPLIT_BY_CHAR,
                METHOD_SPLIT_BY_STRING,
                METHOD_REGEX_SPLIT);
    }

    @Override
    public String toString() {
        return "string";
    }

    private static final PropertyReference PROP_LENGTH = new MethodBasedPropertyReference("length", String.class, "length");

    private static final BinaryOperation STRING_ADD_STRING = new BinaryOperation(BinaryOperator.PLUS, SString.instance, SString.instance, SString.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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

    private static final BinaryOperation STRING_ADD_CHAR = new BinaryOperation(BinaryOperator.PLUS, SString.instance, SString.instance, SChar.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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

    private static final BinaryOperation STRING_EQUALS_STRING = new BinaryOperation(BinaryOperator.EQUALS, SBoolean.instance, SString.instance, SString.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            right.release(left);
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Objects.class),
                    "equals",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class), Type.getType(Object.class)),
                    false);
        }
    };

    private static final BinaryOperation STRING_NOT_EQUALS_STRING = new BinaryOperation(BinaryOperator.NOT_EQUALS, SBoolean.instance, SString.instance, SString.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            SString.STRING_EQUALS_STRING.apply(left, right, context, leftType, rightType);
            SBoolean.NOT.value().apply(left, context);
        }
    };

    private static final BinaryOperation STRING_ADD_STRING_CONVERTIBLE = new BinaryOperation(BinaryOperator.PLUS, SString.instance, SString.instance, SStringConvertible.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            SStringConvertible.instance.extractMethod(rightType).compileInvoke(right, context);
            SString.STRING_ADD_STRING.apply(left, right, context, SString.instance, SString.instance);
        }
    };

    private static final BinaryOperation STRING_CONVERTIBLE_ADD_STRING = new BinaryOperation(BinaryOperator.PLUS, SString.instance, SStringConvertible.instance, SString.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            SStringConvertible.instance.extractMethod(leftType).compileInvoke(left, context);
            SString.STRING_ADD_STRING.apply(left, right, context, SString.instance, SString.instance);
        }
    };

    private static final IndexOperation INDEX_INT = new IndexOperation(SInt.instance, SChar.instance) {
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
    };

    private static final MethodReference METHOD_SUBSTRING_INT = new InstanceMethodReference(
            String.class,
            SString.instance,
            "substring",
            SString.instance,
            new MethodParameter("beginIndex", SInt.instance));

    private static final MethodReference METHOD_SUBSTRING_INT_INT = new InstanceMethodReference(
            String.class,
            SString.instance,
            "substring",
            SString.instance,
            new MethodParameter("beginIndex", SInt.instance),
            new MethodParameter("endIndex", SInt.instance));

    private static final MethodReference METHOD_CONTAINS = new MethodReference() {

        @Override
        public SType getOwner() {
            return instance;
        }

        @Override
        public String getName() {
            return "contains";
        }

        @Override
        public SType getReturn() {
            return SBoolean.instance;
        }

        @Override
        public List<MethodParameter> getParameters() {
            return List.of(new MethodParameter("str", SString.instance));
        }

        @Override
        public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "contains",
                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(CharSequence.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_INDEX_OF = new InstanceMethodReference(
            String.class,
            SString.instance,
            "indexOf",
            SInt.instance,
            new MethodParameter("str", SString.instance));

    private static final MethodReference METHOD_STARTS_WITH = new InstanceMethodReference(
            String.class,
            SString.instance,
            "startsWith",
            SBoolean.instance,
            new MethodParameter("prefix", SString.instance));

    private static final MethodReference METHOD_ENDS_WITH = new InstanceMethodReference(
            String.class,
            SString.instance,
            "endsWith",
            SBoolean.instance,
            new MethodParameter("suffix", SString.instance));

    private static final MethodReference METHOD_TO_LOWER = new MethodReference() {

        @Override
        public SType getOwner() {
            return instance;
        }

        @Override
        public String getName() {
            return "toLower";
        }

        @Override
        public SType getReturn() {
            return SString.instance;
        }

        @Override
        public List<MethodParameter> getParameters() {
            return List.of();
        }

        @Override
        public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
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
        public SType getOwner() {
            return instance;
        }

        @Override
        public String getName() {
            return "toUpper";
        }

        @Override
        public SType getReturn() {
            return SString.instance;
        }

        @Override
        public List<MethodParameter> getParameters() {
            return List.of();
        }

        @Override
        public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
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

    private static final MethodReference METHOD_MATCHES = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "matches",
            SBoolean.instance,
            new MethodParameter("regex", SString.instance));

    private static final MethodReference METHOD_MATCHES_FLAGS = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "matches",
            SBoolean.instance,
            new MethodParameter("regex", SString.instance),
            new MethodParameter("flags", SInt.instance));

    private static final MethodReference METHOD_GET_MATCHES = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "getMatches",
            new SArrayType(SString.instance),
            new MethodParameter("regex", SString.instance));

    private static final MethodReference METHOD_GET_MATCHES_FLAGS = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "getMatches",
            new SArrayType(SString.instance),
            new MethodParameter("regex", SString.instance),
            new MethodParameter("flags", SInt.instance));

    private static final MethodReference METHOD_REPLACE = new MethodReference() {

        @Override
        public SType getOwner() {
            return instance;
        }

        @Override
        public String getName() {
            return "replace";
        }

        @Override
        public SType getReturn() {
            return SString.instance;
        }

        @Override
        public List<MethodParameter> getParameters() {
            return List.of(
                    new MethodParameter("target", SString.instance),
                    new MethodParameter("replacement", SString.instance));
        }

        @Override
        public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    "replace",
                    Type.getMethodDescriptor(Type.getType(String.class), Type.getType(CharSequence.class), Type.getType(CharSequence.class)),
                    false);
        }
    };

    private static final MethodReference METHOD_SPLIT_BY_CHAR = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "split",
            new SArrayType(SString.instance),
            new MethodParameter("separator", SChar.instance));

    private static final MethodReference METHOD_SPLIT_BY_STRING = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "split",
            new SArrayType(SString.instance),
            new MethodParameter("separator", SString.instance));

    private static final MethodReference METHOD_REGEX_SPLIT = new StaticAsInstanceMethodReference(
            StringUtils.class,
            SString.instance,
            "regexSplit",
            new SArrayType(SString.instance),
            new MethodParameter("regex", SString.instance));
}