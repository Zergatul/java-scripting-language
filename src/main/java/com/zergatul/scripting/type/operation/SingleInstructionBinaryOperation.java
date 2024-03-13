package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public class SingleInstructionBinaryOperation extends BinaryOperation {

    private final int opcode;

    public SingleInstructionBinaryOperation(SType type, int opcode) {
        super(type);
        this.opcode = opcode;
    }

    @Override
    public void apply(MethodVisitor left, BufferedMethodVisitor right) {
        right.release(left);
        left.visitInsn(opcode);
    }
}