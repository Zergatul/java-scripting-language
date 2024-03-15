package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SFloatType;
import com.zergatul.scripting.type.SIntType;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class FloatOperations {
    public static final BinaryOperation ADD = new SingleInstructionBinaryOperation(SFloatType.instance, DADD);
    public static final BinaryOperation SUB = new SingleInstructionBinaryOperation(SFloatType.instance, DSUB);
    public static final BinaryOperation MUL = new SingleInstructionBinaryOperation(SFloatType.instance, DMUL);
    public static final BinaryOperation DIV = new SingleInstructionBinaryOperation(SFloatType.instance, DDIV);
    public static final BinaryOperation MOD = new SingleInstructionBinaryOperation(SFloatType.instance, DREM);
    public static final BinaryOperation LT = new FloatComparisonOperation(IF_ICMPLT);
    public static final BinaryOperation GT = new FloatComparisonOperation(IF_ICMPGT);
    public static final BinaryOperation LTE = new FloatComparisonOperation(IF_ICMPLE);
    public static final BinaryOperation GTE = new FloatComparisonOperation(IF_ICMPGE);
    public static final BinaryOperation EQ = new FloatComparisonOperation(IF_ICMPEQ);
    public static final BinaryOperation NEQ = new FloatComparisonOperation(IF_ICMPNE);
    public static final UnaryOperation PLUS = new UnaryOperation(SFloatType.instance) {
        @Override
        public void apply(MethodVisitor visitor) {}
    };
    public static final UnaryOperation MINUS = new UnaryOperation(SFloatType.instance) {
        @Override
        public void apply(MethodVisitor visitor) {
            visitor.visitInsn(DNEG);
        }
    };
}