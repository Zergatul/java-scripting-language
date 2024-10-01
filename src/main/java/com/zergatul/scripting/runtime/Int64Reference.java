package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class Int64Reference {

    private long value;

    public Int64Reference(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public void set(long value) {
        this.value = value;
    }
}