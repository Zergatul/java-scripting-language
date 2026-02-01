package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.MethodHandleCache;
import com.zergatul.scripting.symbols.LocalVariable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
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
    public void compileLoad(MethodVisitor visitor, CompilerContext context, Runnable compileCallee) {
        if (isPublic()) {
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
        } else {
            if (isStatic()) {
                String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        varHandleFieldName,
                        Type.getDescriptor(VarHandle.class));
                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(VarHandle.class),
                        "get",
                        Type.getMethodDescriptor(
                                Type.getType(field.getType())),
                        false);
            } else {
                String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        varHandleFieldName,
                        Type.getDescriptor(VarHandle.class));
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
    }

    @Override
    public void compileStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileValue) {
        if (isPublic()) {
            compileCallee.run();
            compileValue.run();
            if (isStatic()) {
                visitor.visitFieldInsn(
                        PUTSTATIC,
                        Type.getInternalName(field.getDeclaringClass()),
                        field.getName(),
                        Type.getDescriptor(field.getType()));
            } else {
                visitor.visitFieldInsn(
                        PUTFIELD,
                        Type.getInternalName(field.getDeclaringClass()),
                        field.getName(),
                        Type.getDescriptor(field.getType()));
            }
        } else {
            if (isStatic()) {
                String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        varHandleFieldName,
                        Type.getDescriptor(VarHandle.class));
                compileValue.run();
                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(VarHandle.class),
                        "set",
                        Type.getMethodDescriptor(
                                Type.VOID_TYPE,
                                Type.getType(field.getType())),
                        false);
            } else {
                String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        varHandleFieldName,
                        Type.getDescriptor(VarHandle.class));
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
    }

    @Override
    public void compileLoadModifyStore(MethodVisitor visitor, CompilerContext context, Runnable compileCallee, Runnable compileModify) {
        if (isPublic()) {
            if (isStatic()) {
                visitor.visitFieldInsn(
                        GETSTATIC,
                        Type.getInternalName(field.getDeclaringClass()),
                        field.getName(),
                        Type.getDescriptor(field.getType()));
                // ..., odlValue
                compileModify.run();
                // ..., newValue
                visitor.visitFieldInsn(
                        PUTSTATIC,
                        Type.getInternalName(field.getDeclaringClass()),
                        field.getName(),
                        Type.getDescriptor(field.getType()));
            } else {
                compileCallee.run();
                // ..., callee
                visitor.visitInsn(DUP);
                // ..., callee, callee
                visitor.visitFieldInsn(
                        GETFIELD,
                        Type.getInternalName(field.getDeclaringClass()),
                        field.getName(),
                        Type.getDescriptor(field.getType()));
                // ..., callee, odlValue
                compileModify.run();
                // ..., callee, newValue
                visitor.visitFieldInsn(
                        PUTFIELD,
                        Type.getInternalName(field.getDeclaringClass()),
                        field.getName(),
                        Type.getDescriptor(field.getType()));
            }
        } else {
            if (isStatic()) {
                String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        varHandleFieldName,
                        Type.getDescriptor(VarHandle.class));
                // ..., varHandle
                visitor.visitInsn(DUP);
                // ..., varHandle, varHandle
                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(VarHandle.class),
                        "get",
                        Type.getMethodDescriptor(
                                Type.getType(field.getType())),
                        false);
                // ..., varHandle, oldValue
                compileModify.run();
                // ..., varHandle, newValue
                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(VarHandle.class),
                        "set",
                        Type.getMethodDescriptor(
                                Type.VOID_TYPE,
                                Type.getType(field.getType())),
                        false);
            } else {
                context = context.createChild();
                String varHandleFieldName = context.createCachedPrivateFieldHandle(this);
                visitor.visitFieldInsn(
                        GETSTATIC,
                        MethodHandleCache.INTERNAL_NAME,
                        varHandleFieldName,
                        Type.getDescriptor(VarHandle.class));
                // ..., varHandle
                visitor.visitInsn(DUP);
                // ..., varHandle, varHandle
                compileCallee.run();
                // ..., varHandle, varHandle, callee
                visitor.visitInsn(DUP);
                // ..., varHandle, varHandle, callee, callee
                LocalVariable calleeVariable = new LocalVariable(null, SType.fromJavaType(field.getDeclaringClass()), null);
                context.setStackIndex(calleeVariable);
                calleeVariable.compileStore(context, visitor);
                // ..., varHandle, varHandle, callee
                visitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        Type.getInternalName(VarHandle.class),
                        "get",
                        Type.getMethodDescriptor(
                                Type.getType(field.getType()),
                                Type.getType(field.getDeclaringClass())),
                        false);
                // ..., varHandle, oldValue
                compileModify.run();
                // ..., varHandle, newValue
                LocalVariable valueVariable = new LocalVariable(null, SType.fromJavaType(field.getType()), null);
                context.setStackIndex(valueVariable);
                valueVariable.compileStore(context, visitor);
                // ..., varHandle
                calleeVariable.compileLoad(context, visitor);
                // ..., varHandle, callee
                valueVariable.compileLoad(context, visitor);
                // ..., varHandle, callee, newValue
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
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldPropertyReference other) {
            return other.field.equals(field);
        } else {
            return false;
        }
    }

    private boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }
}