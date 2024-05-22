package com.zergatul.scripting.runtime;

@FunctionalInterface
public interface Function1<R, T> {
    R invoke(T param);
}