package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.runtime.ArrayUtils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SArrayType extends SType {

    private final SType underlying;

    public SArrayType(SType underlying) {
        this.underlying = underlying;
    }

    public SType getElementsType() {
        return underlying;
    }

    @Override
    public Class<?> getJavaClass() {
        return underlying.getJavaClass().arrayType();
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
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitInsn(ICONST_0);
        if (underlying.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(underlying.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPredefinedType) underlying).getArrayTypeInst());
        }
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return List.of(PROP_LENGTH.value());
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        return switch (name) {
            case "length" -> PROP_LENGTH.value();
            default -> null;
        };
    }

    @Override
    public List<SType> supportedIndexers() {
        return List.of(SInt.instance);
    }

    @Override
    public IndexOperation index(SType type) {
        if (type == SInt.instance) {
            return new ArrayIndexOperation(getElementsType());
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SArrayType other) {
            return underlying.equals(other.underlying);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public BinaryOperation add(SType other) {
        if (other.equals(underlying)) {
            return new AddElementOperation(this);
        }
        if (other.equals(this)) {
            return new AddArrayOperation(this);
        }
        return null;
    }

    @Override
    public String toString() {
        return underlying.toString() + "[]";
    }

    private static final Lazy<PropertyReference> PROP_LENGTH = new Lazy<>(() -> new PropertyReference() {

        @Override
        public String getName() {
            return "length";
        }

        @Override
        public SType getType() {
            return SInt.instance;
        }

        @Override
        public boolean canGet() {
            return true;
        }

        @Override
        public boolean canSet() {
            return false;
        }

        @Override
        public void compileGet(MethodVisitor visitor) {
            visitor.visitInsn(ARRAYLENGTH);
        }
    });

    private static class AddElementOperation extends BinaryOperation {

        public AddElementOperation(SArrayType arrayType) {
            super(BinaryOperator.PLUS, arrayType, arrayType, arrayType.underlying);
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            boolean isPrimitive = ((SArrayType) type).getElementsType().getJavaClass().isPrimitive();
            Class<?> arrayClass = isPrimitive ? type.getJavaClass() : Object[].class;
            Class<?> elementClass = isPrimitive ? ((SArrayType) type).getElementsType().getJavaClass() : Object.class;
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(ArrayUtils.class),
                    "concat",
                    Type.getMethodDescriptor(Type.getType(arrayClass), Type.getType(arrayClass), Type.getType(elementClass)),
                    false);
            if (!isPrimitive) {
                left.visitTypeInsn(CHECKCAST, Type.getInternalName(type.getJavaClass()));
            }
        }
    }

    private static class AddArrayOperation extends BinaryOperation {

        public AddArrayOperation(SArrayType arrayType) {
            super(BinaryOperator.PLUS, arrayType, arrayType, arrayType);
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            right.release(left);
            boolean isPrimitive = ((SArrayType) type).getElementsType().getJavaClass().isPrimitive();
            Class<?> clazz = isPrimitive ? type.getJavaClass() : Object[].class;
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(ArrayUtils.class),
                    "concat",
                    Type.getMethodDescriptor(Type.getType(clazz), Type.getType(clazz), Type.getType(clazz)),
                    false);
            if (!isPrimitive) {
                left.visitTypeInsn(CHECKCAST, Type.getInternalName(type.getJavaClass()));
            }
        }
    }

    private static class ArrayIndexOperation extends IndexOperation {

        public ArrayIndexOperation(SType type) {
            super(SInt.instance, type);
        }

        @Override
        public boolean canGet() {
            return true;
        }

        @Override
        public boolean canSet() {
            return true;
        }

        @Override
        public void compileGet(MethodVisitor visitor) {
            visitor.visitInsn(returnType.getArrayLoadInst());
        }

        @Override
        public void compileSet(MethodVisitor visitor) {
            visitor.visitInsn(returnType.getArrayStoreInst());
        }
    }
}