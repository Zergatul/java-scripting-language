package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class StringStorage {

    public final List<String> list = new ArrayList<>();

    public void add(String value) {
        list.add(value);
    }
}