package com.zergatul.scripting.symbols;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import com.zergatul.scripting.type.SReference;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class LambdaLiftedLocalVariable extends Variable {

    private final Variable variable;

    public LambdaLiftedLocalVariable(Variable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
    }

    @Override
    public void addReference(BoundNameExpressionNode name) {
        variable.addReference(name);
    }

    public Variable getUnderlyingVariable() {
        return variable;
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
    public void compileInit(CompilerContext context, MethodVisitor visitor) {
        if (!(variable instanceof LocalVariable localVariable)) {
            throw new InternalException("TODO");
        }

        SReference refType = getReferenceType();
        String refClassDescriptor = Type.getInternalName(refType.getJavaClass());

        // ...
        visitor.visitTypeInsn(NEW, refClassDescriptor);
        // ..., Ref
        visitor.visitInsn(DUP);
        // ..., Ref, Ref
        variable.getType().storeDefaultValue(visitor);
        // ..., Ref, Ref, default
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                refClassDescriptor,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(variable.getType().getJavaClass())),
                false);
        // ..., Ref
        visitor.visitVarInsn(refType.getStoreInst(), localVariable.getStackIndex());
        // ...
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        compileReferenceLoad(visitor);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getReferenceType().getJavaClass()),
                "get",
                Type.getMethodDescriptor(Type.getType(getType().getJavaClass())),
                false);
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        compileReferenceLoad(visitor);
        StackHelper.swap(visitor, context, getType(), getReferenceType());
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(getReferenceType().getJavaClass()),
                "set",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(getType().getJavaClass())),
                false);
    }

    public void compileReferenceLoad(MethodVisitor visitor) {
        if (!(variable instanceof LocalVariable localVariable)) {
            throw new InternalException("TODO");
        }

        visitor.visitVarInsn(getReferenceType().getLoadInst(), localVariable.getStackIndex());
    }

    private SReference getReferenceType() {
        return variable.getType().getReferenceType();
    }
}