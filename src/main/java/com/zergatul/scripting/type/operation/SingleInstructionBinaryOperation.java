package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferFunctionVisitor;
import com.zergatul.scripting.compiler.FunctionVisitor;
import com.zergatul.scripting.type.SType;

public class SingleInstructionBinaryOperation extends BinaryOperation {

    private final int opcode;

    public SingleInstructionBinaryOperation(SType type, int opcode) {
        super(type);
        this.opcode = opcode;
    }

    @Override
    public void apply(FunctionVisitor left, BufferFunctionVisitor right) {
        right.release(left);
        left.visitInsn(opcode);
    }
}