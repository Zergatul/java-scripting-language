package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SFloatType;

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
}