package com.zergatul.scripting.type;

public class UnknownPropertyReference extends PropertyReference {

    public static final PropertyReference instance = new UnknownPropertyReference();

    private UnknownPropertyReference() {

    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public SType getType() {
        return SUnknown.instance;
    }

    @Override
    public boolean canLoad() {
        return true;
    }

    @Override
    public boolean canStore() {
        return true;
    }
}