package com.zergatul.scripting.type;

import com.zergatul.scripting.PropertyDescription;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.MethodHandleCache;
import com.zergatul.scripting.symbols.LocalVariable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

public final class FieldPropertyReference extends PropertyReference {

    private final Field field;

    public FieldPropertyReference(Field field) {
        this.field = field;
    }

    public Field getUnderlyingField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public SType getType() {
        return SType.fromJavaType(field.getType());
    }

    @Override
    public Visibility getVisibility() {
        if (Modifier.isPublic(field.getModifiers())) {
            return Visibility.PUBLIC;
        } else if (Modifier.isProtected(field.getModifiers())) {
            return Visibility.PROTECTED;
        } else {
            return Visibility.PRIVATE;
        }
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    @Override
    public boolean canLoad() {
        return true;
    }

    @Override
    public boolean canStore() {
        return !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public Optional<String> getDescription() {
        PropertyDescription description = field.getAnnotation(PropertyDescription.class);
        return description != null ? Optional.of(description.value()) : Optional.empty();
    }

    @Override
    public void compileLoad(MethodVisitor visitor, CompilerContext context, Runnable compileCallee) {
        if (isStatic()) {
            visitor.visitFieldInsn(
                    GETSTATIC,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
        } else {
            compileCallee.run();
            visitor.visitFieldInsn(
                    GETFIELD,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
        }
    }

    @Override
    public void compileVarHandleLoad(MethodVisitor visitor, CompilerContext context, Runnable compileCallee) {
        String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
        visitor.visitFieldInsn(
                GETSTATIC,
                MethodHandleCache.INTERNAL_NAME,
                varHandleFieldName,
                Type.getDescriptor(VarHandle.class));
        if (isStatic()) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "get",
                    Type.getMethodDescriptor(Type.getType(field.getType())),
                    false);
        } else {
            compileCallee.run();
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "get",
                    Type.getMethodDescriptor(
                            Type.getType(field.getType()),
                            Type.getType(field.getDeclaringClass())),
                    false);
        }
    }

    @Override
    public void compileStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileValue) {
        if (isStatic()) {
            compileValue.run();
            visitor.visitFieldInsn(
                    PUTSTATIC,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
        } else {
            compileCallee.run();
            compileValue.run();
            visitor.visitFieldInsn(
                    PUTFIELD,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
        }
    }

    @Override
    public void compileVarHandleStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileValue) {
        String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
        visitor.visitFieldInsn(
                GETSTATIC,
                MethodHandleCache.INTERNAL_NAME,
                varHandleFieldName,
                Type.getDescriptor(VarHandle.class));
        if (isStatic()) {
            compileValue.run();
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "set",
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(field.getType())),
                    false);
        } else {
            compileCallee.run();
            compileValue.run();
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "set",
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.getType(field.getDeclaringClass()),
                            Type.getType(field.getType())),
                    false);
        }
    }

    @Override
    public void compileLoadModifyStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileModify) {
        if (isStatic()) {
            visitor.visitFieldInsn(
                    GETSTATIC,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
            compileModify.run();
            visitor.visitFieldInsn(
                    PUTSTATIC,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
        } else {
            compileCallee.run();
            visitor.visitInsn(DUP);
            visitor.visitFieldInsn(
                    GETFIELD,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
            compileModify.run();
            visitor.visitFieldInsn(
                    PUTFIELD,
                    Type.getInternalName(field.getDeclaringClass()),
                    field.getName(),
                    Type.getDescriptor(field.getType()));
        }
    }

    @Override
    public void compileVarHandleLoadModifyStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileModify) {
        String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
        visitor.visitFieldInsn(
                GETSTATIC,
                MethodHandleCache.INTERNAL_NAME,
                varHandleFieldName,
                Type.getDescriptor(VarHandle.class));
        visitor.visitInsn(DUP);
        if (isStatic()) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "get",
                    Type.getMethodDescriptor(Type.getType(field.getType())),
                    false);
            compileModify.run();
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "set",
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(field.getType())),
                    false);
        } else {
            context = context.createChild();
            compileCallee.run();
            visitor.visitInsn(DUP);
            LocalVariable calleeVariable = new LocalVariable(null, SType.fromJavaType(field.getDeclaringClass()), null);
            context.setStackIndex(calleeVariable);
            calleeVariable.compileStore(context, visitor);
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "get",
                    Type.getMethodDescriptor(
                            Type.getType(field.getType()),
                            Type.getType(field.getDeclaringClass())),
                    false);
            compileModify.run();
            LocalVariable valueVariable = new LocalVariable(null, SType.fromJavaType(field.getType()), null);
            context.setStackIndex(valueVariable);
            valueVariable.compileStore(context, visitor);
            calleeVariable.compileLoad(context, visitor);
            valueVariable.compileLoad(context, visitor);
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "set",
                    Type.getMethodDescriptor(
                            Type.VOID_TYPE,
                            Type.getType(field.getDeclaringClass()),
                            Type.getType(field.getType())),
                    false);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldPropertyReference other) {
            return other.field.equals(field);
        } else {
            return false;
        }
    }
}