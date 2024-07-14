package com.zergatul.scripting.compiler;

import com.zergatul.scripting.binding.AsyncLiftedLocalVariable;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class CapturedAsyncStateMachineFieldVariable extends Variable {

    private final AsyncLiftedLocalVariable variable;
    private String className;
    private String fieldName;

    public CapturedAsyncStateMachineFieldVariable(AsyncLiftedLocalVariable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
    }


    public void setField(String className, String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
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
        loadStateMachineInstance(context, visitor);
        visitor.visitFieldInsn(GETFIELD, className, fieldName, Type.getDescriptor(variable.getType().getJavaClass()));
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        loadStateMachineInstance(context, visitor);
        StackHelper.swap(visitor, context, variable.getType(), SType.fromJavaType(Object.class));
        visitor.visitFieldInsn(PUTFIELD, className, fieldName, Type.getDescriptor(variable.getType().getJavaClass()));
    }

    private void loadStateMachineInstance(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, context.getCurrentClassName(), "capturedAsyncStateMachine", "L" + className + ";");
    }
}