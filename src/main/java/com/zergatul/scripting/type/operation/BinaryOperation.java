package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class BinaryOperation {

    public final BinaryOperator operator;
    public final SType type;

    protected BinaryOperation(BinaryOperator operator, SType type) {
        this.operator = operator;
        this.type = type;
    }

    public abstract void apply(MethodVisitor left, BufferedMethodVisitor right);
}