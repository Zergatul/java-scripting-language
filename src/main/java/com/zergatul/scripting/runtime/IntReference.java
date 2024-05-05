package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class IntReference {

    private int value;

    public IntReference(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }
}