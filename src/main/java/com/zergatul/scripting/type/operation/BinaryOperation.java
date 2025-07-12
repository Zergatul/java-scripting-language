package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class BinaryOperation {

    public final BinaryOperator operator;
    public final SType type;

    private final SType left;
    private final SType right;

    protected BinaryOperation(BinaryOperator operator, SType type, SType left, SType right) {
        this.operator = operator;
        this.type = type;
        this.left = left;
        this.right = right;
    }

    public SType getLeft() {
        return left;
    }

    public SType getRight() {
        return right;
    }

    public abstract void apply(MethodVisitor left, BufferedMethodVisitor right);
}