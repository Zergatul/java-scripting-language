package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferedMethodVisitor;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class BinaryOperation {

    private final BinaryOperator operator;
    private final SType resultType;
    private final SType left;
    private final SType right;

    protected BinaryOperation(BinaryOperator operator, SType resultType, SType left, SType right) {
        this.operator = operator;
        this.resultType = resultType;
        this.left = left;
        this.right = right;
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public SType getResultType() {
        return resultType;
    }

    public SType getLeft() {
        return left;
    }

    public SType getRight() {
        return right;
    }

    public abstract void apply(MethodVisitor left, BufferedMethodVisitor right, CompilerContext context, SType leftType, SType rightType);
}