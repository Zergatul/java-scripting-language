package com.zergatul.scripting.utility;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@NullMarked
public final class Lists {

    @SuppressWarnings("unchecked")
    public static <T> List<T> copyOf(List<? extends T> list) {
        return (List<T>) new ImmutableList<>(list.toArray());
    }

    public static <T> List<T> of() {
        return Collections.emptyList();
    }

    public static <T> List<T> of(T item) {
        return Collections.singletonList(item);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> of(T item1, T item2) {
        return (List<T>) of(new Object[] { item1, item2 });
    }

    @SafeVarargs
    public static <T> List<T> of(T... items) {
        return new ImmutableList<>(items);
    }

    public static <T> List<T> from(T[] items) {
        return new ImmutableList<>(items);
    }

    @SafeVarargs
    public static <E> List<E> from(List<E> list, E... elements) {
        ArrayList<E> result = new ArrayList<>(list.size() + elements.length);
        result.addAll(list);
        result.addAll(Arrays.asList(elements));
        return result;
    }

    @SafeVarargs
    public static <E> List<E> from(List<E> list1, List<E> list2, E... elements) {
        ArrayList<E> result = new ArrayList<>(list1.size() + list2.size() + elements.length);
        result.addAll(list1);
        result.addAll(list2);
        result.addAll(Arrays.asList(elements));
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> from(Stream<T> items) {
        return (List<T>) new ImmutableList<>(items.toArray());
    }
}