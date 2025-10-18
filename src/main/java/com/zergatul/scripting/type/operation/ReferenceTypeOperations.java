package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SBoolean;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ReferenceTypeOperations {

    public static final Lazy<BinaryOperation> EQUALS = new Lazy<>(() -> new ReferenceTypeOperation(BinaryOperator.EQUALS, IF_ACMPEQ));
    public static final Lazy<BinaryOperation> NOT_EQUALS = new Lazy<>(() -> new ReferenceTypeOperation(BinaryOperator.EQUALS, IF_ACMPNE));

    private static class ReferenceTypeOperation extends BinaryOperation {

        private final int opcode;

        public ReferenceTypeOperation(BinaryOperator operator, int opcode) {
            super(operator, SBoolean.instance, SUnknown.instance, SUnknown.instance); // TODO: find better way???
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
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
}