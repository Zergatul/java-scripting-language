package com.zergatul.scripting.runtime;

@FunctionalInterface
public interface Action2<T1, T2> {
    void invoke(T1 param1, T2 param2);
}