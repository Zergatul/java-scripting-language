package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class Float32Storage {

    public final List<Float> list = new ArrayList<>();

    public void add(float value) {
        list.add(value);
    }
}