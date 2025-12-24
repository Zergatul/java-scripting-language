package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class UndefinedUnaryOperation extends UnaryOperation {

    public static final UnaryOperation instance = new UndefinedUnaryOperation();

    private UndefinedUnaryOperation() {
        super(UnaryOperator.PLUS, SUnknown.instance, SUnknown.instance);
    }

    @Override
    public void apply(MethodVisitor visitor, CompilerContext context) {
        throw new InternalException();
    }
}