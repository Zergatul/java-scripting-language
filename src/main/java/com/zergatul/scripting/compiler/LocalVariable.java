package com.zergatul.scripting.compiler;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class LocalVariable extends Variable {

    private final int stackIndex;
    private int asyncState;

    public LocalVariable(String name, SType type, int stackIndex, TextRange definition) {
        super(name, type, definition);
        this.stackIndex = stackIndex;
    }

    public int getStackIndex() {
        return stackIndex;
    }

    public int getAsyncState() {
        return asyncState;
    }

    public void setAsyncState(int state) {
        asyncState = state;
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