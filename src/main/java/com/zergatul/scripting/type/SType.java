package com.zergatul.scripting.type;

import com.zergatul.scripting.InterfaceHelper;
import com.zergatul.scripting.InternalException;
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

    public boolean isPredefined() {
        return false;
    }

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
            return other.isAssignableFrom(this);
        } else {
            return false;
        }
    }

    public boolean isAssignableFrom(SType other) {
        return this.getJavaClass().isAssignableFrom(other.getJavaClass());
    }

    public boolean isCompatibleWith(SType other) {
        return other.isInstanceOf(this);
    }

    public boolean isAbstract() {
        return false;
    }

    public List<BinaryOperation> getBinaryOperations() {
        return List.of();
    }

    public List<UnaryOperation> getUnaryOperations() {
        return List.of();
    }

    public @Nullable PostfixOperation increment() {
        return null;
    }

    public @Nullable PostfixOperation decrement() {
        return null;
    }

    public List<CastOperation> getImplicitCasts() {
        return List.of();
    }

    public static @Nullable CastOperation implicitCastTo(SType src, SType dst) {
        if (src == SUnknown.instance || dst == SUnknown.instance) {
            return UndefinedCastOperation.instance;
        }

        if (src == SNull.instance && !dst.isSyntheticType() && dst.isReference()) {
            return new CastOperation(SNull.instance, dst) {
                @Override
                public void apply(MethodVisitor visitor) {}
            };
        }

        for (CastOperation cast : src.getImplicitCasts()) {
            if (cast.getDstType().isInstanceOf(dst)) {
                return cast;
            }
        }

        return null;
    }

    public List<SType> supportedIndexers() {
        return List.of();
    }

    public @Nullable IndexOperation index(SType type) {
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

    public @Nullable PropertyReference getInstanceProperty(String name) {
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

    public void loadClassObject(MethodVisitor visitor) {
        visitor.visitLdcInsn(Type.getType(getJavaClass()));
    }

    public @Nullable SByReference getReferenceType() {
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
            if (clazz == boolean.class) {
                return SBoolean.instance;
            }
            if (clazz == Boolean.class) {
                return SBoolean.instance.getBoxed();
            }
            if (clazz == byte.class) {
                return SInt8.instance;
            }
            if (clazz == Byte.class) {
                return SInt8.instance.getBoxed();
            }
            if (clazz == short.class) {
                return SInt16.instance;
            }
            if (clazz == Short.class) {
                return SInt16.instance.getBoxed();
            }
            if (clazz == int.class) {
                return SInt.instance;
            }
            if (clazz == Integer.class) {
                return SInt.instance.getBoxed();
            }
            if (clazz == long.class) {
                return SInt64.instance;
            }
            if (clazz == Long.class) {
                return SInt64.instance.getBoxed();
            }
            if (clazz == float.class) {
                return SFloat32.instance;
            }
            if (clazz == Float.class) {
                return SFloat32.instance.getBoxed();
            }
            if (clazz == double.class) {
                return SFloat.instance;
            }
            if (clazz == Double.class) {
                return SFloat.instance.getBoxed();
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