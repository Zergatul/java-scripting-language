package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SBoolean;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BooleanOperations {
    public static final BinaryOperation OR = new SingleInstructionBinaryOperation(SBoolean.instance, IOR);
    public static final BinaryOperation AND = new SingleInstructionBinaryOperation(SBoolean.instance, IAND);
    public static final UnaryOperation NOT = new UnaryOperation(SBoolean.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            Label elseLabel = new Label();
            Label endLabel = new Label();
            visitor.visitJumpInsn(IFNE, elseLabel);
            visitor.visitInsn(ICONST_1);
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            visitor.visitInsn(ICONST_0);
            visitor.visitLabel(endLabel);
        }
    };
}