package com.zergatul.scripting.symbols;

import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class AsyncLiftedLocalVariable extends Variable {

    private final Variable variable;
    private String className;
    private String fieldName;

    public AsyncLiftedLocalVariable(Variable variable) {
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
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, className, fieldName, Type.getDescriptor(variable.getType().getJavaClass()));
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        StackHelper.swap(visitor, context, variable.getType(), SType.fromJavaType(Object.class));
        visitor.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(variable.getType().getJavaClass()));
    }

    public String getClassName() {
        return className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setField(String className, String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }
}