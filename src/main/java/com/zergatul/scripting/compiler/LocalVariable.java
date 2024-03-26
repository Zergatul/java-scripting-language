package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class LocalVariable extends Variable {

    private final int stackIndex;

    public LocalVariable(String name, SType type, int stackIndex) {
        super(name, type);
        this.stackIndex = stackIndex;
    }

    public int getStackIndex() {
        return stackIndex;
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
    public void compileLoad(MethodVisitor visitor) {
        visitor.visitVarInsn(getType().getLoadInst(), stackIndex);
    }

    @Override
    public void compileStore(MethodVisitor visitor) {
        visitor.visitVarInsn(getType().getStoreInst(), stackIndex);
    }
}