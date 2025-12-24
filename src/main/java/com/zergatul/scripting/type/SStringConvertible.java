package com.zergatul.scripting.type;

public class SStringConvertible extends SSyntheticInterface {

    public static final SStringConvertible instance = new SStringConvertible();

    private SStringConvertible() {
        super(new MethodDefinition(SString.instance, "toString"));
    }
}