package com.zergatul.scripting.tests.framework;

public abstract class ComparatorTest {

    protected static Comparator comparator;

    static {
        comparator = new Comparator(ComparatorRegistry.create());
    }
}