package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SBoxedType;
import com.zergatul.scripting.type.SValueType;
import org.objectweb.asm.MethodVisitor;

public class BoxingCastOperation extends CastOperation {

    private final SValueType raw;

    public BoxingCastOperation(SValueType raw, SBoxedType boxed) {
        super(boxed);
        this.raw = raw;
    }

    @Override
    public void apply(MethodVisitor visitor) {
        raw.compileBoxing(visitor);
    }
}