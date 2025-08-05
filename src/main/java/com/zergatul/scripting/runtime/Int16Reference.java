package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class Int16Reference {

    private short value;

    public Int16Reference(short value) {
        this.value = value;
    }

    public short get() {
        return value;
    }

    public void set(short value) {
        this.value = value;
    }
}