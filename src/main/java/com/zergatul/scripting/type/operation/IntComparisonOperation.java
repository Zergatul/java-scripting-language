package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferFunctionVisitor;
import com.zergatul.scripting.compiler.FunctionVisitor;
import com.zergatul.scripting.type.SBoolean;
import org.objectweb.asm.Label;

import static org.objectweb.asm.Opcodes.*;

public class IntComparisonOperation extends BinaryOperation {

    private final int opcode;

    public IntComparisonOperation(int opcode) {
        super(SBoolean.instance);
        this.opcode = opcode;
    }

    @Override
    public void apply(FunctionVisitor left, BufferFunctionVisitor right) {
        right.release(left);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        left.visitJumpInsn(opcode, elseLabel);
        left.visitInsn(ICONST_0);
        left.visitJumpInsn(GOTO, endLabel);
        left.visitLabel(elseLabel);
        left.visitInsn(ICONST_1);
        left.visitLabel(endLabel);
    }
}