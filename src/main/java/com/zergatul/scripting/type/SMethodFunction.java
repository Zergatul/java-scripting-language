package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

public class SMethodFunction extends SFunction {

    public SMethodFunction(SType returnType, MethodParameter[] parameters) {
        super(returnType, parameters);
    }

    @Override
    public Class<?> getJavaClass() {
        throw new InternalException();
    }
}