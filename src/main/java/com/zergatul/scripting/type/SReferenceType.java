package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.AASTORE;

public abstract class SReferenceType extends SType {

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public @Nullable SType getBaseType() {
        return SJavaObject.instance;
    }

    @Override
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
    }

    @Override
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return List.of(
                new ObjectComparisonOperation(SJavaObject.instance, BinaryOperator.EQUALS, SJavaObject.instance, IF_ACMPEQ),
                new ObjectComparisonOperation(SJavaObject.instance, BinaryOperator.NOT_EQUALS, SJavaObject.instance, IF_ACMPNE),
                new NullCoalescingOperation(this));
    }

    private static class ObjectComparisonOperation extends BinaryOperation {

        private final int opcode;

        public ObjectComparisonOperation(SType left, BinaryOperator operator, SType right, int opcode) {
            super(operator, SBoolean.instance, left, right);
            this.opcode = opcode;
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
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

    private static class NullCoalescingOperation extends BinaryOperation {

        public NullCoalescingOperation(SReferenceType type) {
            super(BinaryOperator.NULL_COALESCING, type, type, type);
        }

        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            Label end = new Label();

            // ..., left
            left.visitInsn(DUP);
            // ..., left, left
            left.visitJumpInsn(IFNONNULL, end);
            // ..., left
            left.visitInsn(POP);
            // ...
            right.release(left);
            // ..., right

            left.visitLabel(end);
        }
    }
}