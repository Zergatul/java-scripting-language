package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class FloatReference {

    private double value;

    public FloatReference(double value) {
        this.value = value;
    }

    public double get() {
        return value;
    }

    public void set(double value) {
        this.value = value;
    }
}