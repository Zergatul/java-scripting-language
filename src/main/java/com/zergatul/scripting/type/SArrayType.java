package com.zergatul.scripting.type;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.runtime.ArrayUtils;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import com.zergatul.scripting.utility.Lists;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SArrayType extends SReferenceType {

    private final SType underlying;

    public SArrayType(SType underlying) {
        this.underlying = underlying;
    }

    public SType getElementsType() {
        return underlying;
    }

    @Override
    public Class<?> getJavaClass() {
        return Array.newInstance(underlying.getJavaClass(), 0).getClass();
    }

    @Override
    public boolean isSyntheticType() {
        return underlying.isSyntheticType();
    }

    @Override
    public boolean hasDefaultValue() {
        return true;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        visitor.visitInsn(ICONST_0);
        if (underlying instanceof SValueType) {
            SValueType valueType = (SValueType) underlying;
            visitor.visitIntInsn(NEWARRAY, valueType.getArrayTypeInst());
        } else {
            visitor.visitTypeInsn(ANEWARRAY, underlying.getInternalName());
        }
    }

    @Override
    public List<PropertyReference> getDeclaredProperties() {
        return Lists.of(PROP_LENGTH.value());
    }

    @Override
    public List<IndexOperation> getIndexOperations() {
        return Lists.of(new ArrayIndexOperation(getElementsType()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SArrayType) {
            SArrayType other = (SArrayType) obj;
            return underlying.equals(other.underlying);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return Lists.of(
                new AddElementOperation(this),
                new AddArrayOperation(this));
    }

    @Override
    public String toString() {
        return String.format("%s[]", underlying);
    }

    @Override
    public String asMethodPart() {
        return "array$" + underlying.asMethodPart() + "$";
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
        public boolean canLoad() {
            return true;
        }

        @Override
        public boolean canStore() {
            return false;
        }

        @Override
        public void compileLoad(MethodVisitor visitor, CompilerContext context, Runnable compileCallee) {
            compileCallee.run();
            visitor.visitInsn(ARRAYLENGTH);
        }
    });

    private static class AddElementOperation extends BinaryOperation {

        public AddElementOperation(SArrayType arrayType) {
            super(BinaryOperator.PLUS, arrayType, arrayType, arrayType.underlying);
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            right.release(left);
            boolean isPrimitive = ((SArrayType) getResultType()).getElementsType().getJavaClass().isPrimitive();
            Class<?> arrayClass = isPrimitive ? getResultType().getJavaClass() : Object[].class;
            Class<?> elementClass = isPrimitive ? ((SArrayType) getResultType()).getElementsType().getJavaClass() : Object.class;
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(ArrayUtils.class),
                    "concat",
                    Type.getMethodDescriptor(Type.getType(arrayClass), Type.getType(arrayClass), Type.getType(elementClass)),
                    false);
            if (!isPrimitive) {
                left.visitTypeInsn(CHECKCAST, getResultType().getInternalName());
            }
        }
    }

    private static class AddArrayOperation extends BinaryOperation {

        public AddArrayOperation(SArrayType arrayType) {
            super(BinaryOperator.PLUS, arrayType, arrayType, arrayType);
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            right.release(left);
            boolean isPrimitive = ((SArrayType) getResultType()).getElementsType().getJavaClass().isPrimitive();
            Class<?> clazz = isPrimitive ? getResultType().getJavaClass() : Object[].class;
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(ArrayUtils.class),
                    "concat",
                    Type.getMethodDescriptor(Type.getType(clazz), Type.getType(clazz), Type.getType(clazz)),
                    false);
            if (!isPrimitive) {
                left.visitTypeInsn(CHECKCAST, getResultType().getInternalName());
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