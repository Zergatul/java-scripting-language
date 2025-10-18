package com.zergatul.scripting.type;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.Setter;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class SClassType extends SType {

    private final Class<?> clazz;

    public SClassType(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getJavaClass() {
        return this.clazz;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
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
    public boolean isReference() {
        return true;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        if (other.isReference()) {
            return EQUALS.value();
        } else {
            return super.equalsOp(other);
        }
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        if (other.isReference()) {
            return NOT_EQUALS.value();
        } else {
            return super.equalsOp(other);
        }
    }

    @Override
    public List<ConstructorReference> getConstructors() {
        return Arrays.stream(clazz.getConstructors())
                .map(NativeConstructorReference::new)
                .map(c -> (ConstructorReference) c)
                .toList();
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .map(FieldPropertyReference::new)
                .map(f -> (PropertyReference) f)
                .toList();
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                return null;
            }
            return new FieldPropertyReference(field);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return Arrays.stream(this.clazz.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                // return Object members only for Object class
                .filter(m -> clazz == Object.class || m.getDeclaringClass() != Object.class)
                .map(NativeInstanceMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    @Override
    public List<MethodReference> getStaticMethods() {
        return Arrays.stream(this.clazz.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> !m.isAnnotationPresent(Getter.class))
                .filter(m -> !m.isAnnotationPresent(Setter.class))
                .map(NativeStaticMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    @Override
    public List<PropertyReference> getStaticProperties() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .map(StaticFieldPropertyReference::new)
                .map(r -> (PropertyReference) r)
                .toList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SClassType other) {
            return other.clazz == clazz;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Java<%s>", clazz.getName());
    }

    private static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() ->
            new ObjectComparisonOperation(BinaryOperator.EQUALS, IF_ACMPEQ));

    private static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() ->
            new ObjectComparisonOperation(BinaryOperator.NOT_EQUALS, IF_ACMPNE));

    private static class ObjectComparisonOperation extends BinaryOperation {

        private final int opcode;

        public ObjectComparisonOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SInt.instance, SInt.instance);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
            right.release(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }
}