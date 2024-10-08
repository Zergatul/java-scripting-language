package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class UnaryOperation {

    public final UnaryOperator operator;
    public final SType type;

    protected UnaryOperation(UnaryOperator operator, SType type) {
        this.operator = operator;
        this.type = type;
    }

    public abstract void apply(MethodVisitor visitor);
}