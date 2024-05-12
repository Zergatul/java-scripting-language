package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.parser.PostfixOperator;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class PostfixOperation {

    public final PostfixOperator operator;
    public final SType type;

    protected PostfixOperation(PostfixOperator operator, SType type) {
        this.operator = operator;
        this.type = type;
    }

    public abstract void apply(MethodVisitor visitor);
}