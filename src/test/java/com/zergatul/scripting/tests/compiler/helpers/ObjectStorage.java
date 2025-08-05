package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class ObjectStorage {

    public final List<Object> list = new ArrayList<>();

    public void add(Object value) {
        list.add(value);
    }
}