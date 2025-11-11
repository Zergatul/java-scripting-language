package com.zergatul.scripting.type;

import com.zergatul.scripting.InterfaceHelper;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.*;
import com.zergatul.scripting.type.operation.*;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class SType {

    public abstract Class<?> getJavaClass();
    public abstract boolean hasDefaultValue();
    public abstract void storeDefaultValue(MethodVisitor visitor);
    public abstract int getLoadInst();
    public abstract int getStoreInst();
    public abstract int getArrayLoadInst();
    public abstract int getArrayStoreInst();
    public abstract boolean isReference();
    public abstract int getReturnInst();

    public boolean isJvmCategoryOneComputationalType() {
        return true;
    }

    public String getInternalName() {
        return Type.getInternalName(getJavaClass());
    }

    public Type getAsmType() {
        return Type.getType(getJavaClass());
    }

    public String getDescriptor() {
        return Type.getDescriptor(getJavaClass());
    }

    // returns true is type doesn't have corresponding Java type
    public boolean isSyntheticType() {
        return false;
    }

    public boolean isInstanceOf(SType other) {
        if (equals(other)) {
            return true;
        }
        if (isSyntheticType() || other.isSyntheticType()) {
            return false;
        }
        if (this == SVoidType.instance || other == SVoidType.instance) {
            return false;
        }
        if (isReference() && other.isReference()) {
            return other.getJavaClass().isAssignableFrom(getJavaClass());
        } else {
            return false;
        }
    }

    public boolean isAbstract() {
        return false;
    }

    @Nullable
    public BinaryOperation add(SType other) {
        if (other == SString.instance) {
            Optional<BinaryOperation> operation = SString.instance.genericLeftAdd(this);
            if (operation.isPresent()) {
                return operation.get();
            }
        }

        return null;
    }

    @Nullable
    public BinaryOperation subtract(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation multiply(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation divide(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation modulo(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation lessThan(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation greaterThan(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation lessEquals(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation greaterEquals(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation equalsOp(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation notEqualsOp(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation bitwiseAnd(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation bitwiseOr(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation booleanAnd(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation booleanOr(SType other) {
        return null;
    }

    @Nullable
    public BinaryOperation binary(BinaryOperator operator, SType other) {
        return switch (operator) {
            case PLUS -> add(other);
            case MINUS -> subtract(other);
            case MULTIPLY -> multiply(other);
            case DIVIDE -> divide(other);
            case MODULO -> modulo(other);
            case BITWISE_OR -> bitwiseOr(other);
            case BITWISE_AND -> bitwiseAnd(other);
            case BOOLEAN_OR -> booleanOr(other);
            case BOOLEAN_AND -> booleanAnd(other);
            case EQUALS -> equalsOp(other);
            case NOT_EQUALS -> notEqualsOp(other);
            case LESS -> lessThan(other);
            case GREATER -> greaterThan(other);
            case LESS_EQUALS -> lessEquals(other);
            case GREATER_EQUALS -> greaterEquals(other);
            case IS, AS, IN -> throw new InternalException();
        };
    }

    @Nullable
    public UnaryOperation plus() {
        return null;
    }

    @Nullable
    public UnaryOperation minus() {
        return null;
    }

    @Nullable
    public UnaryOperation not() {
        return null;
    }

    @Nullable
    public UnaryOperation unary(UnaryOperator operator) {
        return switch (operator) {
            case PLUS -> plus();
            case MINUS -> minus();
            case NOT -> not();
        };
    }

    @Nullable
    public PostfixOperation increment() {
        return null;
    }

    @Nullable
    public PostfixOperation decrement() {
        return null;
    }

    @Nullable
    protected CastOperation implicitCastTo(SType other) {
        return null;
    }

    @Nullable
    public static CastOperation implicitCastTo(SType src, SType dst) {
        if (src == SUnknown.instance || dst == SUnknown.instance) {
            return EmptyCastOperation.instance;
        }
        return src.implicitCastTo(dst);
    }

    public List<SType> supportedIndexers() {
        return List.of();
    }

    @Nullable
    public IndexOperation index(SType type) {
        return null;
    }

    public List<ConstructorReference> getConstructors() {
        return List.of();
    }

    public List<MethodReference> getInstanceMethods() {
        return List.of();
    }

    public List<MethodReference> getStaticMethods() {
        return List.of();
    }

    public List<PropertyReference> getInstanceProperties() {
        return List.of();
    }

    @Nullable
    public PropertyReference getInstanceProperty(String name) {
        return null;
    }

    public List<PropertyReference> getStaticProperties() {
        return List.of();
    }

    public Optional<MethodReference> getToStringMethod() {
        return getInstanceMethods().stream()
                .filter(m -> m.getName().equals("toString"))
                .filter(m -> m.getParameters().isEmpty())
                .filter(m -> m.getReturn() == SString.instance)
                .findFirst();
    }

    @Nullable
    public Class<?> getBoxedVersion() {
        return null;
    }

    public void compileBoxing(MethodVisitor visitor) {
        throw new InternalException();
    }

    public void compileUnboxing(MethodVisitor visitor) {
        throw new InternalException();
    }

    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitLdcInsn(Type.getType(getJavaClass()));
    }

    @Nullable
    public SByReference getReferenceType() {
        return null;
    }

    public Class<?> getReferenceClass() {
        throw new InternalException();
    }

    public String asMethodPart() {
        StringBuilder builder = new StringBuilder();
        for (char ch : toString().toCharArray()) {
            if (Character.isJavaIdentifierPart(ch)) {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    public static SType fromJavaType(java.lang.reflect.Type type) {
        if (type instanceof TypeVariable<?> typeVariable) {
            return fromJavaType(typeVariable.getBounds()[0]);
        }

        if (type instanceof ParameterizedType parameterized) {
            java.lang.reflect.Type raw = parameterized.getRawType();
            if (raw instanceof Class<?> clazz) {
                if (InterfaceHelper.isFuncInterface(clazz)) {
                    return SFunctionalInterface.from(parameterized);
                }
            }

            java.lang.reflect.Type[] arguments = parameterized.getActualTypeArguments();
            if (parameterized.getRawType() == CompletableFuture.class) {
                return new SFuture(fromJavaType(arguments[0]));
            }

            return new SClassType((Class<?>) parameterized.getRawType());
        }

        if (type instanceof WildcardType wildcard) {
            return fromJavaType(wildcard.getUpperBounds()[0]);
        }

        if (type instanceof GenericArrayType genericArray) {
            return new SArrayType(fromJavaType(genericArray.getGenericComponentType()));
        }

        if (type instanceof Class<?> clazz) {
            if (clazz == void.class) {
                return SVoidType.instance;
            }
            if (clazz == Void.class) {
                return SVoidType.instance;
            }
            if (clazz == boolean.class || clazz == Boolean.class) {
                return SBoolean.instance;
            }
            if (clazz == byte.class || clazz == Byte.class) {
                return SInt8.instance;
            }
            if (clazz == short.class || clazz == Short.class) {
                return SInt16.instance;
            }
            if (clazz == int.class || clazz == Integer.class) {
                return SInt.instance;
            }
            if (clazz == long.class || clazz == Long.class) {
                return SInt64.instance;
            }
            if (clazz == float.class || clazz == Float.class) {
                return SFloat32.instance;
            }
            if (clazz == double.class || clazz == Double.class) {
                return SFloat.instance;
            }
            if (clazz == String.class) {
                return SString.instance;
            }
            if (clazz == IntReference.class) {
                return SByReference.INT;
            }
            if (clazz == Int64Reference.class) {
                return SByReference.INT64;
            }
            if (clazz == FloatReference.class) {
                return SByReference.FLOAT;
            }
            if (clazz.isArray()) {
                return new SArrayType(fromJavaType(clazz.getComponentType()));
            }
            if (clazz == Object.class) {
                return SJavaObject.instance;
            }
            if (InterfaceHelper.isFuncInterface(clazz)) {
                return SFunctionalInterface.from(clazz);
            }
            if (clazz.isAnnotationPresent(CustomType.class)) {
                return new SCustomType(clazz);
            }
            return new SClassType(clazz);
        }

        throw new InternalException();
    }
}