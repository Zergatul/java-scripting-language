package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.operation.BinaryOperation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class SNull extends SSyntheticType {

    public static final SNull instance = new SNull();

    private SNull() {}

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public boolean isInstanceOf(SType other) {
        if (other == this) {
            return true;
        }
        if (other.isSyntheticType()) {
            return false;
        }
        return other.isReference();
    }

    @Override
    public List<BinaryOperation> getBinaryOperations() {
        return List.of(
                NULL_EQUALS_NULL,
                NULL_NOT_EQUALS_NULL,
                NULL_EQUALS_VALUE_TYPE,
                VALUE_TYPE_EQUALS_NULL,
                NULL_NOT_EQUALS_VALUE_TYPE,
                VALUE_TYPE_NOT_EQUALS_NULL);
    }

    @Override
    public String toString() {
        return "null";
    }

    private static final BinaryOperation NULL_EQUALS_NULL = new BinaryOperation(BinaryOperator.EQUALS, SBoolean.instance, SNull.instance, SNull.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            left.visitInsn(Opcodes.POP);
            // discard right
            left.visitInsn(Opcodes.ICONST_1);
        }
    };

    private static final BinaryOperation NULL_NOT_EQUALS_NULL = new BinaryOperation(BinaryOperator.NOT_EQUALS, SBoolean.instance, SNull.instance, SNull.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            left.visitInsn(Opcodes.POP);
            // discard right
            left.visitInsn(Opcodes.ICONST_0);
        }
    };

    private static final BinaryOperation NULL_EQUALS_VALUE_TYPE = new BinaryOperation(BinaryOperator.EQUALS, SBoolean.instance, SNull.instance, SValueTypeConstraint.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            right.release(left);
            left.visitInsn(rightType.isJvmCategoryOneComputationalType() ? Opcodes.POP : Opcodes.POP2);
            left.visitInsn(Opcodes.POP);
            left.visitInsn(Opcodes.ICONST_0);
        }
    };

    private static final BinaryOperation VALUE_TYPE_EQUALS_NULL = new BinaryOperation(BinaryOperator.EQUALS, SBoolean.instance, SValueTypeConstraint.instance, SNull.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            left.visitInsn(leftType.isJvmCategoryOneComputationalType() ? Opcodes.POP : Opcodes.POP2);
            right.release(left);
            left.visitInsn(Opcodes.POP);
            left.visitInsn(Opcodes.ICONST_0);
        }
    };

    private static final BinaryOperation NULL_NOT_EQUALS_VALUE_TYPE = new BinaryOperation(BinaryOperator.NOT_EQUALS, SBoolean.instance, SNull.instance, SValueTypeConstraint.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            right.release(left);
            left.visitInsn(rightType.isJvmCategoryOneComputationalType() ? Opcodes.POP : Opcodes.POP2);
            left.visitInsn(Opcodes.POP);
            left.visitInsn(Opcodes.ICONST_1);
        }
    };

    private static final BinaryOperation VALUE_TYPE_NOT_EQUALS_NULL = new BinaryOperation(BinaryOperator.NOT_EQUALS, SBoolean.instance, SValueTypeConstraint.instance, SNull.instance) {
        @Override
        public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType) {
            left.visitInsn(leftType.isJvmCategoryOneComputationalType() ? Opcodes.POP : Opcodes.POP2);
            right.release(left);
            left.visitInsn(Opcodes.POP);
            left.visitInsn(Opcodes.ICONST_1);
        }
    };
}