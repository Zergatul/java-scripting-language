package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class AnnotatedPropertyReference extends PropertyReference {

    private final String name;
    private final SType type;
    private final @Nullable Method getter;
    private final @Nullable Method setter;

    public AnnotatedPropertyReference(String name, SType type, @Nullable Method getter, @Nullable Method setter) {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public SType getType() {
        return type;
    }

    @Override
    public boolean canLoad() {
        return getter != null;
    }

    @Override
    public boolean canStore() {
        return setter != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor, Runnable compileCallee) {
        if (getter == null) {
            throw new InternalException();
        }

        compileCallee.run();
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getter.getDeclaringClass()),
                getter.getName(),
                Type.getMethodDescriptor(getter),
                false);
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor, Runnable compileCallee, Runnable compileValue) {
        if (setter == null) {
            throw new InternalException();
        }

        compileCallee.run();
        compileValue.run();
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(setter.getDeclaringClass()),
                setter.getName(),
                Type.getMethodDescriptor(setter),
                false);
    }

    @Override
    public void compileLoadModifyStore(CompilerContext context, MethodVisitor visitor, Runnable compileCallee, Runnable compileModify) {
        if (getter == null || setter == null) {
            throw new InternalException();
        }

        compileCallee.run();
        visitor.visitInsn(DUP);

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getter.getDeclaringClass()),
                getter.getName(),
                Type.getMethodDescriptor(getter),
                false);

        compileModify.run();

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(setter.getDeclaringClass()),
                setter.getName(),
                Type.getMethodDescriptor(setter),
                false);
    }
}