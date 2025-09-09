package com.zergatul.scripting.type;

public class SEmptyCollection extends SSyntheticType {

    public static final SEmptyCollection instance = new SEmptyCollection();

    private SEmptyCollection() {}

    @Override
    public String toString() {
        return "[]";
    }
}