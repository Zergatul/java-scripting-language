package com.zergatul.scripting.symbols;

import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import com.zergatul.scripting.type.SReference;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class CapturedLocalVariable extends Variable {

    private final Variable variable;
    private String fieldName;
    private String className;

    protected CapturedLocalVariable(Variable variable) {
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, className, fieldName, Type.getDescriptor(getReferenceType().getJavaClass()));
    }

    private SReference getReferenceType() {
        return getType().getReferenceType();
    }
}
