package com.zergatul.scripting.runtime;

@FunctionalInterface
public interface Function0<R> {
    R invoke();
}