package com.zergatul.scripting.type;

@FunctionalInterface
public interface Action1<T> {
    void invoke(T param1);
}