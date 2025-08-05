package com.zergatul.scripting.tests.completion.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lists {

    @SafeVarargs
    public static <E> List<E> of(List<E> list, E... elements) {
        ArrayList<E> result = new ArrayList<>(list.size() + elements.length);
        result.addAll(list);
        result.addAll(Arrays.asList(elements));
        return result;
    }

    @SafeVarargs
    public static <E> List<E> of(List<E> list1, List<E> list2, E... elements) {
        ArrayList<E> result = new ArrayList<>(list1.size() + list2.size() + elements.length);
        result.addAll(list1);
        result.addAll(list2);
        result.addAll(Arrays.asList(elements));
        return result;
    }
}