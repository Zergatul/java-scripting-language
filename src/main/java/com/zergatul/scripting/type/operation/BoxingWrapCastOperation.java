package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SValueType;
import org.objectweb.asm.MethodVisitor;

public class BoxingWrapCastOperation extends CastOperation {

    private final CastOperation inner;

    public BoxingWrapCastOperation(CastOperation inner) {
        super(inner.getSrcType(), getBoxed(inner));
        this.inner = inner;
    }

    @Override
    public void apply(MethodVisitor visitor) {
        inner.apply(visitor);
        ((SValueType) inner.getDstType()).compileBoxing(visitor);
    }

    private static SType getBoxed(CastOperation cast) {
        if (cast.getDstType() instanceof SValueType valueType) {
            return valueType.getBoxed();
        }
        throw new InternalException();
    }
}
