package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class BooleanReference {

    private boolean value;

    public BooleanReference(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}