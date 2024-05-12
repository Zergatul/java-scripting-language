package com.zergatul.scripting.compiler;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SReference;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class LocalRefParameter extends LocalVariable {

    private final SReference refType;

    public LocalRefParameter(String name, SReference refType, SType underlying, int stackIndex, TextRange definition) {
        super(name, underlying, stackIndex, definition);
        this.refType = refType;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(refType.getLoadInst(), getStackIndex());
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(refType.getJavaClass()),
                "get",
                Type.getMethodDescriptor(Type.getType(getType().getJavaClass())),
                false);
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(refType.getLoadInst(), getStackIndex());
        StackHelper.swap(visitor, context, getType(), refType);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(refType.getJavaClass()),
                "set",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(getType().getJavaClass())),
                false);
    }
}