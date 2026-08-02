package com.zergatul.scripting.type;

public class SUnknown extends SSyntheticType {

    public static final SUnknown instance = new SUnknown();

    private SUnknown() {}

    @Override
    public boolean canBeGeneric() {
        return true;
    }

    @Override
    public boolean canApplyGenericArgumentsCount(int count) {
        return true;
    }

    @Override
    public SType withGenericArguments(SType... arguments) {
        return this;
    }

    @Override
    public String toString() {
        return "<Unknown>";
    }
}