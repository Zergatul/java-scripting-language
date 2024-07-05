package com.zergatul.scripting.compiler;

import org.objectweb.asm.MethodVisitor;

public class CapturedLocalVariable extends Variable {

    private final Variable variable;

    protected CapturedLocalVariable(Variable variable) {
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
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        throw new RuntimeException();
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        throw new RuntimeException();
    }
}
