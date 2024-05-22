package com.zergatul.scripting.runtime;

@FunctionalInterface
public interface Function2<R, T1, T2> {
    R invoke(T1 param1, T2 param2);
}