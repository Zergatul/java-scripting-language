package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class EmptyCastOperation extends CastOperation {

    public static final CastOperation instance = new EmptyCastOperation();

    private EmptyCastOperation() {
        super(SUnknown.instance);
    }

    @Override
    public void apply(MethodVisitor visitor) {}
}