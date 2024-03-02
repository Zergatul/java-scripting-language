package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.compiler.BufferFunctionVisitor;
import com.zergatul.scripting.compiler.FunctionVisitor;
import com.zergatul.scripting.type.SType;

public abstract class BinaryOperation {

    public final SType type;

    public abstract void apply(FunctionVisitor left, BufferFunctionVisitor right);

    protected BinaryOperation(SType type) {
        this.type = type;
    }
}