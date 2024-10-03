package com.zergatul.scripting;

import java.util.function.Supplier;

public class Lazy<T> {

    private Supplier<T> supplier;
    private T value;
    private boolean initialized;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
        this.initialized = false;
    }

    public synchronized T value() {
        if (!initialized) {
            value = supplier.get();
            initialized = true;
            supplier = null; // allow GC to collect the supplier if needed
        }
        return value;
    }
}