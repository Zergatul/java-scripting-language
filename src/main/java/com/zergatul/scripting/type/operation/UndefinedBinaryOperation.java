package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class UndefinedBinaryOperation extends BinaryOperation {

    public static final BinaryOperation instance = new UndefinedBinaryOperation();

    private UndefinedBinaryOperation() {
        super(BinaryOperator.PLUS, SUnknown.instance, SUnknown.instance, SUnknown.instance);
    }

    @Override
    public void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context) {
        throw new InternalException();
    }
}