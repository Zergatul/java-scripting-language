package com.zergatul.scripting.symbols;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.generator.StateBoundary;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LocalVariable extends Variable {

    private int stackIndex;
    private StateBoundary state;

    public LocalVariable(String name, SType type, TextRange definition) {
        super(name, type, definition);
        stackIndex = Integer.MIN_VALUE;
    }

    public int getStackIndex() {
        return stackIndex;
    }

    public void setStackIndex(int stackIndex) {
        this.stackIndex = stackIndex;
    }

    public StateBoundary getGeneratorState() {
        return state;
    }

    public void setGeneratorState(StateBoundary state) {
        this.state = state;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean canSet() {
        return true;
    }

    @Override
    public void compileLoad(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(getType().getLoadInst(), stackIndex);
    }

    @Override
    public void compileStore(CompilerContext context, MethodVisitor visitor) {
        visitor.visitVarInsn(getType().getStoreInst(), stackIndex);
    }
}