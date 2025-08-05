package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class Int8Reference {

    private byte value;

    public Int8Reference(byte value) {
        this.value = value;
    }

    public byte get() {
        return value;
    }

    public void set(byte value) {
        this.value = value;
    }
}