package com.zergatul.scripting.symbols;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.StackHelper;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

public class LiftedVariable extends Variable {

    private final LocalVariable variable;
    private LocalVariable closure;
    private String className;
    private String fieldName;

    public LiftedVariable(LocalVariable variable) {
        super(variable.getName(), variable.getType(), variable.getDefinition());
        this.variable = variable;
    }

    public LocalVariable getUnderlying() {
        return variable;
    }

    @Override
    public boolean canSet() {
        return variable.canSet();
    }

    @Override
    public boolean isConstant() {
        return variable.isConstant();
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        closure.compileLoad(context, visitor);
        StackHelper.swap(visitor, context, variable.getType(), closure.getType());
        visitor.visitFieldInsn(PUTFIELD, className, fieldName, variable.getType().getDescriptor());
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        closure.compileLoad(context, visitor);
        visitor.visitFieldInsn(GETFIELD, className, fieldName, variable.getType().getDescriptor());
    }

    public void setField(String className, String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }

    public void setClosure(LocalVariable closure) {
        this.closure = closure;
    }

    public String getClassName() {
        return className;
    }

    public String getFieldName() {
        return fieldName;
    }
}