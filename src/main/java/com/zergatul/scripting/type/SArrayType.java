package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.runtime.ArrayUtils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SArrayType extends SType {

    private final SType type;

    public SArrayType(SType type) {
        this.type = type;
    }

    public SType getElementsType() {
        return type;
    }

    @Override
    public Class<?> getJavaClass() {
        return type.getJavaClass().arrayType();
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
        if (type.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(type.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPredefinedType) type).getArrayTypeInst());
        }
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
            return new ArrayIndexOperation(getElementsType());
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SArrayType other) {
            return type.equals(other.type);
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
        if (other.equals(this)) {
            return new AddArrayOperation(this);
        }
        return null;
    }

    @Override
    public String toString() {
        return type.toString() + "[]";
    }

    private static final PropertyReference PROP_LENGTH = new PropertyReference() {
        @Override
        public SType getType() {
            return SIntType.instance;
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
    };

    private static class AddArrayOperation extends BinaryOperation {

        public AddArrayOperation(SArrayType type) {
            super(type);
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
            super(type);
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
            visitor.visitInsn(type.getArrayLoadInst());
        }

        @Override
        public void compileSet(MethodVisitor visitor) {
            visitor.visitInsn(type.getArrayStoreInst());
        }
    }
}