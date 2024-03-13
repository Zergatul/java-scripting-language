package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

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

    public BinaryOperation floorMod(SType other) {
        return null;
    }

    public BinaryOperation floorDiv(SType other) {
        return null;
    }

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

    public BinaryOperation and(SType other) {
        return null;
    }

    public BinaryOperation or(SType other) {
        return null;
    }

    public BinaryOperation binary(BinaryOperator operator, SType other) {
        return switch (operator) {
            case PLUS -> add(other);
            case MINUS -> subtract(other);
            case MULTIPLY -> multiply(other);
            case DIVIDE -> divide(other);
            case MODULO -> modulo(other);
            case OR -> or(other);
            case AND -> and(other);
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

    public UnaryOperation implicitCastTo(SType other) {
        return null;
    }

    public BinaryOperation index(SType other) {
        return null;
    }

    public List<MethodReference> getInstanceMethods(String name) {
        return List.of();
    }

    public List<MethodReference> getStaticMethods(String name) {
        return List.of();
    }

    /*public MemberReference getInstanceProperty(String name) {
        return false;
    }*/

    public SType compileGetField(String field, MethodVisitor visitor) {
        return null;
    }

    public static SType fromJavaClass(Class<?> type) {
        if (type == void.class) {
            return SVoidType.instance;
        }
        if (type == boolean.class) {
            return SBoolean.instance;
        }
        if (type == int.class) {
            return SIntType.instance;
        }
        if (type == double.class) {
            return SFloatType.instance;
        }
        if (type == String.class) {
            return SStringType.instance;
        }
        if (type.isArray()) {
            return new SArrayType(fromJavaClass(type.getComponentType()));
        }
        if (type.getSuperclass() != null) {
            return new SClassType(type);
        } else {
            return null;
        }
    }
}