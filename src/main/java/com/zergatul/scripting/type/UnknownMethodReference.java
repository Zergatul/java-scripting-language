package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.util.List;

public class UnknownMethodReference extends MethodReference {

    public static final MethodReference instance = new UnknownMethodReference();

    private UnknownMethodReference() {}

    @Override
    public String getName() {
        return "";
    }

    @Override
    public SType getReturn() {
        return SUnknown.instance;
    }

    @Override
    public List<SType> getParameters() {
        throw new InternalException();
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        throw new InternalException();
    }
}