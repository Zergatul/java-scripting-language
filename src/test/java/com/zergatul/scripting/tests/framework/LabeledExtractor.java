package com.zergatul.scripting.tests.framework;

import java.util.Objects;
import java.util.function.Function;

public interface LabeledExtractor<T, R> extends Function<T, R> {
    String label();
    default String pathSegment() { return label(); }

    static <T, R> LabeledExtractor<T, R> of(String label, Function<T, R> fn) {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(fn, "fn");
        return new LabeledExtractor<>() {
            @Override
            public String label() {
                return label;
            }

            @Override
            public R apply(T arg) {
                return fn.apply(arg);
            }
        };
    }
}