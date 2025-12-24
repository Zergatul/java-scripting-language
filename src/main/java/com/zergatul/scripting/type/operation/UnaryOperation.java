package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class UnaryOperation {

    private final UnaryOperator operator;
    private final SType resultType;
    private final SType operandType;

    protected UnaryOperation(UnaryOperator operator, SType resultType, SType operandType) {
        this.operator = operator;
        this.resultType = resultType;
        this.operandType = operandType;
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    public SType getResultType() {
        return resultType;
    }

    public SType getOperandType() {
        return operandType;
    }

    public abstract void apply(MethodVisitor visitor, CompilerContext context);
}