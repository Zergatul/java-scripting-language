package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

public abstract class ConstantStaticVariable extends StaticVariable {

    protected ConstantStaticVariable(String name, SType type) {
        super(name, type);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public void compileStore(MethodVisitor visitor) {
        throw new InternalException();
    }
}