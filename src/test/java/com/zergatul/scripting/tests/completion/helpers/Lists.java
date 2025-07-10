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
}