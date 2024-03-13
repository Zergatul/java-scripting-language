package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.SBoolean;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ICONST_1;

public class FloatComparisonOperation extends BinaryOperation {

    private final int opcode;

    protected FloatComparisonOperation(int opcode) {
        super(SBoolean.instance);
        this.opcode = opcode;
    }

    @Override
    public void apply(MethodVisitor left, BufferedMethodVisitor right) {
        right.release(left);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        left.visitInsn(DCMPG);
        left.visitInsn(ICONST_0);
        left.visitJumpInsn(opcode, elseLabel);
        left.visitInsn(ICONST_0);
        left.visitJumpInsn(GOTO, endLabel);
        left.visitLabel(elseLabel);
        left.visitInsn(ICONST_1);
        left.visitLabel(endLabel);
    }
}