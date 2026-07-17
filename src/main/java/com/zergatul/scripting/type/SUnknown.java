package com.zergatul.scripting.type;

public class SUnknown extends SSyntheticType {

    public static final SUnknown instance = new SUnknown();

    private SUnknown() {}

    @Override
    public String toString() {
        return "<Unknown>";
    }
}