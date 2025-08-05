package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class Int16Storage {

    public final List<Short> list = new ArrayList<>();

    public void add(short value) {
        list.add(value);
    }
}