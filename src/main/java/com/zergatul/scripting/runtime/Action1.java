package com.zergatul.scripting.runtime;

@FunctionalInterface
public interface Action1<T> {
    void invoke(T param1);
}