package com.zergatul.scripting.runtime;

@SuppressWarnings("unused")
public class Float32Reference {

    private float value;

    public Float32Reference(float value) {
        this.value = value;
    }

    public float get() {
        return value;
    }

    public void set(float value) {
        this.value = value;
    }
}