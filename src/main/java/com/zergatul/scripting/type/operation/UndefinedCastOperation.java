package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class UndefinedCastOperation extends CastOperation {

    public static final CastOperation instance = new UndefinedCastOperation();

    private UndefinedCastOperation() {
        super(SUnknown.instance);
    }

    @Override
    public void apply(MethodVisitor visitor) {
        throw new InternalException();
    }
}