package com.zergatul.scripting.type;

public class SJavaObject extends SClassType {

    public static final SJavaObject instance = new SJavaObject();

    private SJavaObject() {
        super(Object.class);
    }
}