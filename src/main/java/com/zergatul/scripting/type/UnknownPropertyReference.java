package com.zergatul.scripting.type;

public class UnknownPropertyReference extends PropertyReference {

    public static final PropertyReference instance = new UnknownPropertyReference();

    private UnknownPropertyReference() {

    }

    @Override
    public SType getType() {
        return SUnknown.instance;
    }

    @Override
    public boolean canGet() {
        return true;
    }

    @Override
    public boolean canSet() {
        return true;
    }
}