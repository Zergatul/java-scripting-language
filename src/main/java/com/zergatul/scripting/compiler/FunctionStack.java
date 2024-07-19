package com.zergatul.scripting.compiler;

public class FunctionStack {

    private int index;

    public int get() {
        return index;
    }

    public void inc(int size) {
        index += size;
    }

    public void set(int index) {
        this.index = index;
    }
}