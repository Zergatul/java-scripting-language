package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

import java.lang.reflect.Method;
import java.util.List;

public class UnknownMethodReference extends MethodReference {

    public static final MethodReference instance = new UnknownMethodReference();

    private UnknownMethodReference() {
        super(null);
    }

    @Override
    public Method getMethod() {
        throw new InternalException();
    }

    @Override
    public List<SType> getParameters() {
        throw new InternalException();
    }

    @Override
    public SType getReturn() {
        return SUnknown.instance;
    }
}