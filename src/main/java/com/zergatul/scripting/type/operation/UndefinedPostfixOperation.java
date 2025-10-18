package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.parser.PostfixOperator;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class UndefinedPostfixOperation extends PostfixOperation {

    public static final PostfixOperation INSTANCE = new UndefinedPostfixOperation();

    private UndefinedPostfixOperation() {
        super(PostfixOperator.PLUS_PLUS, SUnknown.instance);
    }

    @Override
    public void apply(MethodVisitor visitor) {
        throw new InternalException();
    }
}