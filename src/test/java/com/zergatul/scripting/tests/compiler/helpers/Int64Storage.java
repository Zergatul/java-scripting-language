package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class Int64Storage {

    public final List<Long> list = new ArrayList<>();

    public void add(long value) {
        list.add(value);
    }
}