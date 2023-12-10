package com.zergatul.scripting.compiler.variables;

import com.zergatul.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.scripting.compiler.types.SType;

public class LocalVariableEntry extends VariableEntry {

    private final int index;

    public LocalVariableEntry(SType type, int index) {
        super(type);
        this.index = index;
    }

    @Override
    public void compileLoad(CompilerMethodVisitor visitor) {
        visitor.visitVarInsn(type.getLoadInst(), index);
    }

    @Override
    public void compileStore(CompilerMethodVisitor visitor) {
        visitor.visitVarInsn(type.getStoreInst(), index);
    }

    @Override
    public void compileIncrement(CompilerMethodVisitor visitor, int value) {
        visitor.visitIincInsn(index, value);
    }
}