package com.zergatul.scripting.symbols;

import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class CapturedAsyncStateMachineFieldVariable extends Variable {

    private final AsyncLiftedLocalVariable variable;

    public CapturedAsyncStateMachineFieldVariable(AsyncLiftedLocalVariable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
    }

    @Override
    public void addReference(BoundNameExpressionNode name) {
        variable.addReference(name);
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
        visitor.visitFieldInsn(GETFIELD, variable.getClassName(), variable.getFieldName(), Type.getDescriptor(variable.getType().getJavaClass()));
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        loadStateMachineInstance(context, visitor);
        StackHelper.swap(visitor, context, variable.getType(), SType.fromJavaType(Object.class));
        visitor.visitFieldInsn(PUTFIELD, variable.getClassName(), variable.getFieldName(), Type.getDescriptor(variable.getType().getJavaClass()));
    }

    private void loadStateMachineInstance(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, context.getCurrentClassName(), "capturedAsyncStateMachine", "L" + variable.getClassName() + ";");
    }
}