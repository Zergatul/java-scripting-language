package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.type.SBoolean;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class BooleanOperations {
    public static final BinaryOperation BITWISE_OR = new SingleInstructionBinaryOperation(SBoolean.instance, IOR);
    public static final BinaryOperation BITWISE_AND = new SingleInstructionBinaryOperation(SBoolean.instance, IAND);
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
    public static final BinaryOperation BOOLEAN_OR = new BinaryOperation(SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            Label returnTrue = new Label();
            Label end = new Label();
            left.visitJumpInsn(IFNE, returnTrue);
            right.release(left);
            left.visitJumpInsn(GOTO, end);
            left.visitLabel(returnTrue);
            left.visitInsn(ICONST_1);
            left.visitLabel(end);
        }
    };
    public static final BinaryOperation BOOLEAN_AND = new BinaryOperation(SBoolean.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right) {
            Label returnFalse = new Label();
            Label end = new Label();
            left.visitJumpInsn(IFEQ, returnFalse);
            right.release(left);
            left.visitJumpInsn(GOTO, end);
            left.visitLabel(returnFalse);
            left.visitInsn(ICONST_0);
            left.visitLabel(end);
        }
    };
}