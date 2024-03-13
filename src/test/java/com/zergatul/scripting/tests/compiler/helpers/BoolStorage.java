package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class BoolStorage {

    public final List<Boolean> list = new ArrayList<>();

    public void add(boolean value) {
        list.add(value);
    }
}