package com.zergatul.scripting.type;

import com.zergatul.scripting.type.operation.CastOperation;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

public class SNull extends SSyntheticType {

    public static final SNull instance = new SNull();

    private SNull() {}

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public boolean isInstanceOf(SType other) {
        if (other.isSyntheticType()) {
            return false;
        }
        return other.isReference();
    }

    @Override
    protected @Nullable CastOperation implicitCastTo(SType other) {
        if (other.isSyntheticType()) {
            return null;
        }
        if (other.isReference()) {
            return new IdentityCastOperation(other);
        }
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

    private static class IdentityCastOperation extends CastOperation {

        public IdentityCastOperation(SType type) {
            super(type);
        }

        @Override
        public void apply(MethodVisitor visitor) {}
    }
}