package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class EmptyUnaryOperation extends UnaryOperation {

    public static final UnaryOperation instance = new EmptyUnaryOperation();

    private EmptyUnaryOperation() {
        super(UnaryOperator.PLUS, SUnknown.instance);
    }

    @Override
    public void apply(MethodVisitor visitor) {}
}