package com.zergatul.scripting.analysis.hover;

@FunctionalInterface
public interface HoverMapper<T> {
    T map(HoverInfo hover);
}