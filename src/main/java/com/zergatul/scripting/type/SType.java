package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class SType {

    public abstract Class<?> getJavaClass();
    public abstract void storeDefaultValue(MethodVisitor visitor);
    public abstract int getLoadInst();
    public abstract int getStoreInst();
    public abstract int getArrayLoadInst();
    public abstract int getArrayStoreInst();
    public abstract boolean isReference();
    public abstract int getReturnInst();

    public String getDescriptor() {
        return Type.getDescriptor(getJavaClass());
    }

    public BinaryOperation add(SType other) {
        return null;
    }

    public BinaryOperation subtract(SType other) {
        return null;
    }

    public BinaryOperation multiply(SType other) {
        return null;
    }

    public BinaryOperation divide(SType other) {
        return null;
    }

    public BinaryOperation modulo(SType other) {
        return null;
    }

    /*public BinaryOperation floorMod(SType other) {
        return null;
    }

    public BinaryOperation floorDiv(SType other) {
        return null;
    }*/

    public BinaryOperation lessThan(SType other) {
        return null;
    }

    public BinaryOperation greaterThan(SType other) {
        return null;
    }

    public BinaryOperation lessEquals(SType other) {
        return null;
    }

    public BinaryOperation greaterEquals(SType other) {
        return null;
    }

    public BinaryOperation equalsOp(SType other) {
        return null;
    }

    public BinaryOperation notEqualsOp(SType other) {
        return null;
    }

    public BinaryOperation bitwiseAnd(SType other) {
        return null;
    }

    public BinaryOperation bitwiseOr(SType other) {
        return null;
    }

    public BinaryOperation booleanAnd(SType other) {
        return null;
    }

    public BinaryOperation booleanOr(SType other) {
        return null;
    }

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
        };
    }

    public UnaryOperation plus() {
        return null;
    }

    public UnaryOperation minus() {
        return null;
    }

    public UnaryOperation not() {
        return null;
    }

    public UnaryOperation unary(UnaryOperator operator) {
        return switch (operator) {
            case PLUS -> plus();
            case MINUS -> minus();
            case NOT -> not();
        };
    }

    public UnaryOperation increment() {
        return null;
    }

    public UnaryOperation decrement() {
        return null;
    }

    public UnaryOperation implicitCastTo(SType other) {
        return null;
    }

    public List<SType> supportedIndexers() {
        return List.of();
    }

    public IndexOperation index(SType type) {
        return null;
    }

    public List<MethodReference> getInstanceMethods(String name) {
        return List.of();
    }

    public List<MethodReference> getStaticMethods(String name) {
        return List.of();
    }

    public PropertyReference getInstanceProperty(String name) {
        return null;
    }

    public Class<?> getBoxedVersion() {
        return null;
    }

    public void compileUnboxing(MethodVisitor visitor) {
        throw new InternalException();
    }

    public static SType fromJavaType(java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType parameterized) {
            java.lang.reflect.Type[] arguments = parameterized.getActualTypeArguments();
            if (parameterized.getRawType() == Action1.class) {
                return new SAction(fromJavaType(arguments[0]));
            }
            if (parameterized.getRawType() == Action2.class) {
                return new SAction(fromJavaType(arguments[0]), fromJavaType(arguments[1]));
            }

            throw new InternalException("Unsupported parametrized type.");
        }

        if (type instanceof Class<?> clazz) {
            if (clazz == void.class) {
                return SVoidType.instance;
            }
            if (clazz == boolean.class || clazz == Boolean.class) {
                return SBoolean.instance;
            }
            if (clazz == int.class || clazz == Integer.class) {
                return SIntType.instance;
            }
            if (clazz == double.class || clazz == Double.class) {
                return SFloatType.instance;
            }
            if (clazz == String.class) {
                return SStringType.instance;
            }
            if (clazz == Action0.class) {
                return new SAction();
            }
            /*if (type == Action1.class) {
                return new SAction(SUnknown.instance);
            }
            if (type == Action2.class) {
                return new SAction(SUnknown.instance, SUnknown.instance);
            }*/
            if (clazz.isArray()) {
                return new SArrayType(fromJavaType(clazz.getComponentType()));
            }
            if (clazz.getSuperclass() != null || clazz == Object.class) {
                return new SClassType(clazz);
            } else {
                return null;
            }
        }

        return null;
    }
}