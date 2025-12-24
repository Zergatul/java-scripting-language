package com.zergatul.scripting.type;

public class SValueTypeConstraint extends SSyntheticType {

    public static final SValueTypeConstraint instance = new SValueTypeConstraint();

    private SValueTypeConstraint() {}

    @Override
    public boolean isCompatibleWith(SType other) {
        return other instanceof SValueType;
    }
}