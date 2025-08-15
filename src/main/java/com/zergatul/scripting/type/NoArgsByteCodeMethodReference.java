package com.zergatul.scripting.type;

import java.util.List;

public abstract class NoArgsByteCodeMethodReference extends MethodReference {

    private final SType owner;
    private final SType returnType;
    private final String name;

    protected NoArgsByteCodeMethodReference(SType owner, SType returnType, String name) {
        this.owner = owner;
        this.returnType = returnType;
        this.name = name;
    }

    @Override
    public SType getOwner() {
        return owner;
    }

    @Override
    public SType getReturn() {
        return returnType;
    }

    @Override
    public List<MethodParameter> getParameters() {
        return List.of();
    }

    @Override
    public String getName() {
        return name;
    }
}