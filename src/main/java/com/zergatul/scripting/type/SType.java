package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.runtime.*;
import com.zergatul.scripting.type.operation.*;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SType {

    public abstract Class<?> getJavaClass();
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

    public PostfixOperation increment() {
        return null;
    }

    public PostfixOperation decrement() {
        return null;
    }

    public CastOperation implicitCastTo(SType other) {
        return null;
    }

    public List<SType> supportedIndexers() {
        return List.of();
    }

    public IndexOperation index(SType type) {
        return null;
    }

    public List<MethodReference> getInstanceMethods() {
        return List.of();
    }

    public List<MethodReference> getStaticMethods() {
        return List.of();
    }

    public PropertyReference getInstanceProperty(String name) {
        return null;
    }

    public Class<?> getBoxedVersion() {
        return null;
    }

    public void compileBoxing(MethodVisitor visitor) {
        throw new InternalException();
    }

    public void compileUnboxing(MethodVisitor visitor) {
        throw new InternalException();
    }

    public SReference getReferenceType() {
        throw new InternalException();
    }

    public Class<?> getReferenceClass() {
        throw new InternalException();
    }

    public static SType fromJavaType(java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType parameterized) {
            java.lang.reflect.Type[] arguments = parameterized.getActualTypeArguments();
            if (parameterized.getRawType() == Action1.class) {
                return new SLambdaFunction(SVoidType.instance, fromJavaType(arguments[0]));
            }
            if (parameterized.getRawType() == Action2.class) {
                return new SLambdaFunction(SVoidType.instance, fromJavaType(arguments[0]), fromJavaType(arguments[1]));
            }
            if (parameterized.getRawType() == Function0.class) {
                return new SLambdaFunction(fromJavaType(arguments[0]));
            }
            if (parameterized.getRawType() == Function1.class) {
                return new SLambdaFunction(fromJavaType(arguments[0]), fromJavaType(arguments[1]));
            }
            if (parameterized.getRawType() == Function2.class) {
                return new SLambdaFunction(fromJavaType(arguments[0]), fromJavaType(arguments[1]), fromJavaType(arguments[2]));
            }
            if (parameterized.getRawType() == CompletableFuture.class) {
                return new SFuture(fromJavaType(arguments[0]));
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
                return SInt.instance;
            }
            if (clazz == double.class || clazz == Double.class) {
                return SFloat.instance;
            }
            if (clazz == String.class) {
                return SString.instance;
            }
            if (clazz == Action0.class) {
                return new SLambdaFunction(SVoidType.instance);
            }
            if (clazz == IntReference.class) {
                return SReference.INT;
            }
            if (clazz == FloatReference.class) {
                return SReference.FLOAT;
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