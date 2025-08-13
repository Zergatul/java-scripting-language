package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class DeclaredMethodReference extends MethodReference {

    public DeclaredMethodReference(SDeclaredType owner, String name, SType returnType, List<MethodParameter> parameters) {

    }

    @Override
    public SType getOwner() {
        throw new InternalException();
    }

    @Override
    public SType getReturn() {
        throw new InternalException();
    }

    @Override
    public List<MethodParameter> getParameters() {
        throw new InternalException();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public String getName() {
        throw new InternalException();
    }
}