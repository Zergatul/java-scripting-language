package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.BoxingWrapCastOperation;
import com.zergatul.scripting.type.operation.CastOperation;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class SValueType extends SType {

    protected final Class<?> type;

    protected SValueType(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean isPredefined() {
        return true;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public Class<?> getJavaClass() {
        return type;
    }

    public abstract int getArrayTypeInst();
    public abstract SBoxedType getBoxed();
    public abstract void compileBoxing(MethodVisitor visitor);
    public abstract void compileUnboxing(MethodVisitor visitor);

    protected static List<CastOperation> extendWithBoxing(CastOperation... casts) {
        List<CastOperation> result = new ArrayList<>(Arrays.asList(casts));
        for (CastOperation cast : casts) {
            if (cast.getDstType() instanceof SValueType) {
                result.add(new BoxingWrapCastOperation(cast));
            }
        }
        return Collections.unmodifiableList(result);
    }
}