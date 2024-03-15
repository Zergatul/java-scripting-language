package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SFloatType;
import com.zergatul.scripting.type.SIntType;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class IntOperations {
    public static final BinaryOperation ADD = new SingleInstructionBinaryOperation(SIntType.instance, IADD);
    public static final BinaryOperation SUB = new SingleInstructionBinaryOperation(SIntType.instance, ISUB);
    public static final BinaryOperation MUL = new SingleInstructionBinaryOperation(SIntType.instance, IMUL);
    public static final BinaryOperation DIV = new SingleInstructionBinaryOperation(SIntType.instance, IDIV);
    public static final BinaryOperation MOD = new SingleInstructionBinaryOperation(SIntType.instance, IREM);
    public static final BinaryOperation AND = new SingleInstructionBinaryOperation(SIntType.instance, IAND);
    public static final BinaryOperation OR = new SingleInstructionBinaryOperation(SIntType.instance, IOR);
    public static final BinaryOperation LT = new IntComparisonOperation(IF_ICMPLT);
    public static final BinaryOperation GT = new IntComparisonOperation(IF_ICMPGT);
    public static final BinaryOperation LTE = new IntComparisonOperation(IF_ICMPLE);
    public static final BinaryOperation GTE = new IntComparisonOperation(IF_ICMPGE);
    public static final BinaryOperation EQ = new IntComparisonOperation(IF_ICMPEQ);
    public static final BinaryOperation NEQ = new IntComparisonOperation(IF_ICMPNE);
    public static final UnaryOperation PLUS = new UnaryOperation(SIntType.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };
    public static final UnaryOperation MINUS = new UnaryOperation(SIntType.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(INEG);
        }
    };
    public static final UnaryOperation TO_FLOAT = new UnaryOperation(SFloatType.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(I2D);
        }
    };
}