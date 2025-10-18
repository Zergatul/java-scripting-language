package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class UnknownMethodReference extends MethodReference {

    public static final MethodReference instance = new UnknownMethodReference();

    private UnknownMethodReference() {}

    @Override
    public SType getOwner() {
        return SUnknown.instance;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public SType getReturn() {
        return SUnknown.instance;
    }

    @Override
    public List<MethodParameter> getParameters() {
        throw new InternalException();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        throw new InternalException();
    }
}