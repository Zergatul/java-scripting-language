package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SReference;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class LiftedLocalVariable extends Variable {

    private final Variable variable;

    public LiftedLocalVariable(Variable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
    }

    @Override
    public boolean isConstant() {
        return variable.isConstant();
    }

    @Override
    public boolean canSet() {
        return variable.canSet();
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        if (!(variable instanceof LocalVariable localVariable)) {
            throw new InternalException("TODO");
        }

        SReference refType = variable.getType().getReferenceType();
        visitor.visitVarInsn(refType.getLoadInst(), localVariable.getStackIndex());
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(refType.getJavaClass()),
                "get",
                Type.getMethodDescriptor(Type.getType(getType().getJavaClass())),
                false);
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        if (!(variable instanceof LocalVariable localVariable)) {
            throw new InternalException("TODO");
        }

        SReference refType = variable.getType().getReferenceType();
        visitor.visitVarInsn(refType.getLoadInst(), localVariable.getStackIndex());
        StackHelper.swap(visitor, context, getType(), refType);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(refType.getJavaClass()),
                "set",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(getType().getJavaClass())),
                false);
    }
}