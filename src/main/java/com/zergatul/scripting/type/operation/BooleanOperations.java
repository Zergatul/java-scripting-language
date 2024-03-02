package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SBoolean;

import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IOR;

public class BooleanOperations {
    public static final BinaryOperation OR = new SingleInstructionBinaryOperation(SBoolean.instance, IOR);
    public static final BinaryOperation AND = new SingleInstructionBinaryOperation(SBoolean.instance, IAND);
}
