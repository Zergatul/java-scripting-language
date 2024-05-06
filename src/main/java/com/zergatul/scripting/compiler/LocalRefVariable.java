package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SReference;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.SWAP;

public class LocalRefVariable extends LocalVariable {

    private final SReference refType;

    public LocalRefVariable(String name, SReference refType, SType underlying, int stackIndex) {
        super(name, underlying, stackIndex);
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