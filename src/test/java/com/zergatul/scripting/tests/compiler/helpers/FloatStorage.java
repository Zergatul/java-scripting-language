package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class FloatStorage {

    public final List<Double> list = new ArrayList<>();

    public void add(double value) {
        list.add(value);
    }
}