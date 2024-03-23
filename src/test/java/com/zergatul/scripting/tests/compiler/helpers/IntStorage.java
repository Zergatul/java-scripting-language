package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class IntStorage {

    public final List<Integer> list = new ArrayList<>();

    public void add(int value) {
        list.add(value);
    }
}