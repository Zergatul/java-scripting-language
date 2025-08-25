package com.zergatul.scripting.symbols;

import com.zergatul.scripting.type.Invocable;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SFunction;

import java.util.List;

public class InvocableObject implements Invocable {

    private final SFunction function;

    public InvocableObject(SFunction function) {
        this.function = function;
    }

    @Override
    public List<MethodParameter> getParameters() {
        return function.getParameters();
    }
}